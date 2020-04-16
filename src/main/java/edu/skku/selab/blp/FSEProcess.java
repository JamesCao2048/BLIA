package edu.skku.selab.blp;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;


/**
 * TODO: 1.先用已有项目验证comment对方法级别定位有用没
 *  2. 将已有的数据加上comment之后看结果
 *
 */
//将FSE格式的repo转为BLIA格式
class FixMethod {
    public String methodName;
    public String returnType;
    public String parameters;
    public FixMethod(String name, String type, String parameter){
        this.methodName = name;
        this.returnType = type;
        this.parameters = parameter;
    }
}

class FixFile {
    public String fileName;
    public List<FixMethod> fixMethods;
    public FixFile(String name, List<FixMethod> methods){
        fileName = name;
        fixMethods = methods;
    }
}

class FixCommit {
    public String commitId;
    public String author;
    public String date;
    public List<FixFile> fixFiles;
    public FixCommit(String id, String author, String date, List<FixFile> files){
        this.commitId = id;
        this.author = author;
        this.date = date;
        this.fixFiles = files;
    }
}

class BugRepo{
    public String id;
    public String openDate;
    public String fixDate;
    public String summary;
    public String description;
    public List<String> comments;
    public List<FixCommit> fixCommits;
    public BugRepo(String id, String odate, String fdate,
                   String summary, String description, List<String> comments, List<FixCommit> fixCommits){
        this.id = id;
        this.openDate = odate;
        this.fixDate = fdate;
        this.summary = summary;
        this.description = description;
        this.comments = comments;
        this.fixCommits = fixCommits;
    }
}

public class FSEProcess {
    static final String inputDict = "/Users/junming/code/BLIA/data/FSE/";
    static final String outputDict = "/Users/junming/code/BLIA/data/FSE/";
    static final String outputRepo = "swt";
    static final String defaultAuthor = "default";
    static final String defaultDescp = "default";
    static final List<String> defaultComments = new LinkedList<String>();
    public static void main(String[] args) throws Exception{
        String inFile = inputDict+"blia.xlsx";
        //String projectDict = "tomcat/";
        String projectDict = "";
        List<BugRepo> bugs = parseBugRepos(inFile, projectDict,2);
        convertXML(bugs, outputDict+"swt.xml");
        return;
    }
    private static List<BugRepo> parseBugRepos(String inFileName, String projectDict, int sheetIndex) throws Exception{
        List<BugRepo> res = new LinkedList();
        Workbook wb = readExcel(inFileName);
        Sheet sheet = wb.getSheetAt(sheetIndex);
        int rownum = sheet.getPhysicalNumberOfRows();
        for(int i = 1; i< rownum; i++){
            Row row = sheet.getRow(i);
            String id = row.getCell(0).toString().split("\\.")[0];
            String openDate;
            if(row.getCell(1).getCellType() == CellType.STRING){
                openDate = row.getCell(1).toString().split("EDT")[0].trim()+":00";
            }
            else
                openDate = getDateValue(row.getCell(1));
            String fixDate = getDateValue(row.getCell(6));
            String summary = row.getCell(2).toString();
            //System.out.println(i);
            String description = row.getCell(3) == null? defaultDescp: row.getCell(3).toString();
            if(description.trim().length() == 0)
                description = defaultDescp;
            //目前数据集中一个bugrepo只对应一个fix commit
            List<FixCommit> fixCommits = new LinkedList();
            String commitId = row.getCell(5).toString();
            List<FixFile> fixFiles = parseFiles(row.getCell(4).toString(), projectDict);
            fixCommits.add(new FixCommit(commitId, defaultAuthor, fixDate, fixFiles));
            res.add(new BugRepo(id,openDate,fixDate, summary,
                    description, defaultComments, fixCommits));
        }
        return res;
    }

    //TODO: 转换的时候要将method的参数从,分隔改为从空格分隔
    private static void convertXML(List<BugRepo> bugs, String outFileName){
        /*
         * 使用DOM生成XML文档的大致步骤:
         * 1:创建一个Document对象表示一个空文档
         * 2:向Document中添加根元素
         * 3:按照文档应有的结构从根元素开始顺序添加
         *   子元素来形成该文档结构。
         * 4:创建XmlWriter对象
         * 5:将Document对象写出
         *   若写入到文件中则形成一个xml文件
         *   也可以写出到网络中作为传输数据使用
         */

        Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement("bugrepository");
        root.addAttribute("name", outputRepo);

        for(BugRepo bug : bugs){

            Element bugE = root.addElement("bug");
            bugE.addAttribute("id", bug.id);
            bugE.addAttribute("opendate", bug.openDate);
            bugE.addAttribute("fixdate", bug.fixDate);

            Element bugInformation = bugE.addElement("buginformation");
            Element summary = bugInformation.addElement("summary");
            summary.addText(bug.summary);
            Element description = bugInformation.addElement("description");
            description.addText(bug.description);
            Element comments = bugInformation.addElement("comments");
            //TODO: 将来在这里加入comment的详细信息

            Element fixCommits = bugE.addElement("fixedCommits");
            for(FixCommit commit: bug.fixCommits){
                Element c = fixCommits.addElement("commit");
                c.addAttribute("id", commit.commitId);
                c.addAttribute("author", commit.author);
                c.addAttribute("date", commit.date);
                for(FixFile file: commit.fixFiles){
                    Element f = c.addElement("file");
                    f.addAttribute("name", file.fileName);
                    for(FixMethod method: file.fixMethods){
                        Element m = f.addElement("method");
                        m.addAttribute("name", method.methodName);
                        m.addAttribute("returnType", method.returnType);
                        m.addAttribute("parameters", method.parameters.replace(","," "));
                    }
                }
            }

        }
        try{
            XMLWriter writer = new XMLWriter(new OutputStreamWriter(
                    new FileOutputStream(outFileName), "UTF-8"));
            writer.write(doc);
            writer.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private static List<FixFile> parseFiles(String filesData, String projectDict){
        List<FixFile> res = new LinkedList();
        String[] fileStrs = filesData.split(";");
        Map<String, FixFile> fileMap = new HashMap();
        for(String fileStr: fileStrs){
            String[] token = fileStr.split("#");
            StringBuilder fileName = new StringBuilder();
            fileName.append(projectDict);
            int i = 0;
            while(!token[i].contains(".java")){
                fileName.append(token[i]+"/");
                i++;
            }
            fileName.append(token[i]);
            i++;

            List<FixMethod> methods;
            if(fileMap.containsKey(fileName.toString()))
                methods = fileMap.get(fileName.toString()).fixMethods;
            else
                methods = new LinkedList();
            if(i< token.length){
                String returnType = token[i++];
                String methodName = token[i++];
                StringBuilder parameter = new StringBuilder();
                while(i < token.length){
                    parameter.append(token[i++]+" ");
                }
                methods.add(new FixMethod(methodName, returnType, parameter.toString().trim()));
            }

            FixFile file = new FixFile(fileName.toString(), methods);
            fileMap.put(fileName.toString(), file);
        }
        res.addAll(fileMap.values());
        return res;
    }

    public static Workbook readExcel(String filePath){
        Workbook wb = null;
        if(filePath==null){
            return null;
        }
        String extString = filePath.substring(filePath.lastIndexOf("."));
        InputStream is = null;
        try {
            is = new FileInputStream(filePath);
            if(".xls".equals(extString)){
                return wb = new HSSFWorkbook(is);
            }else if(".xlsx".equals(extString)){
                return wb = new XSSFWorkbook(is);
            }else{
                return wb = null;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wb;
    }

    public static String getDateValue(Cell cell){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = cell.getDateCellValue();
            return simpleDateFormat.format(date);
    }

}
