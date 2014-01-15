package org.shanghai.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.Resource;
import java.util.logging.Logger;

import org.shanghai.crawl.FileTransporter;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import java.util.List;
import java.util.Iterator;

import java.io.File;
import java.io.Reader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;

/**
  @license http://www.apache.org/licenses/LICENSE-2.0
  @author Alfred Klaut
  @title Test : Scanner for csv files 
  @date 2013-11-16
*/
public class CSVScanner implements FileTransporter.Delegate {

    private String fileName;
    private String startDirectory;
    private Database database;
    private PreparedStatement statement;

    @Override
    public FileTransporter.Delegate create() {
        database = new Database();
        database.create();
        return this;
    }

    @Override
    public void dispose() {
        database.dispose();
	}

    //@Override
    //public void setDirectory(String dir) {
    //    this.startDirectory = dir;
    //}

    @Override
    public boolean canRead(File file) {
        if (file.getName().endsWith(".csv")) {
            log("canTalk " + file.getName());
            return true;
        }
        return false;
    }

    @Override
    public Model read(String about, String file) {
        Model model = ModelFactory.createDefaultModel();
        Resource resource = model.createResource(about);
        test(file);
        return model;
    }

    private void makeTable() {
        int check = database.getSingleInt("select 1 from nlp");
        if (check==1) {
            log("table nlp exist and was destroyed");
            dropTable();
        } else {
            log("checked " + check);
        }
        dropTable();
        createTable();
    }

    private void dropTable() {
        String query = "drop table if exists nlp";
        database.update(query);
    }

    private void createTable() {
        String query = "create table if not exists nlp ("
               + "oid int primary key auto_increment"
               + ",company text"
               + ",wkn text"
               + ",pubDate text"
               + ",title text"
               + ",statement text"
               + ",location text"
               + ",analyst text"
               + ",name text"
               + ",prename text"
               + ",surname text" //10
               + ",gender varchar(22)"
               + ",oldRecommendation varchar(9)"
               + ",newRecommendation varchar(9)"
               + ",changeRecommendation varchar(9)"
               + ",oldPrice varchar(9)"
               + ",newPrice varchar(9)"
               + ",changePrice varchar(9)"
               + ",currency varchar(9)"
               + ",background text"
               + " CHARACTER SET utf8 COLLATE utf8_bin)";
        //System.out.println(query);
        database.update(query);
    }

    /**
    private void prepare(String what) {
        prepRange = countOccurrences(what,'?');
        try {
           prepStmt = connection.prepareStatement(what); 
	    } catch(SQLException e) { log(e); }
        log("prepared statement range " + prepRange);
    }
    **/

    private void insertToDatabase(CSVRecord record) {
        Iterator<String> it = record.iterator();
        int i=2;
        try {
	        statement.setString(1, null); // first column is identifier
            while(it.hasNext()) {
                String data = it.next();
                if (data!=null)
	                statement.setString(i, data);
		        i++;
            }
            //System.out.println(statement.toString());
	        statement.executeUpdate();
	    } catch(SQLException e) { log(statement.toString()); log(e); }
    }

    private void prepare(int size) {
        StringBuilder sb = new StringBuilder();
        sb.append("insert into nlp values(");
		for (int i=0; i<size; i++) {
			sb.append("?,");
        }
		sb.append("?)");
		statement = database.prepare(sb.toString());
    }

    private void csvToDatabase(File file) {
        if (!canRead(file)) {
            log("can't handle " + file.getName());
            return;
        }

        int count=0;
		boolean prepared = false;
        try {
            Reader in = new java.io.FileReader(file);
            CSVParser parser = new CSVParser(in, CSVFormat.EXCEL);
            List<CSVRecord> list = parser.getRecords();
            for(CSVRecord record : list) {
			    if (!prepared) {
                    prepared = true;
                    prepare(record.size());
                }
                count++;
                if (count>3) //skip header
                    insertToDatabase(record);
            }
        } catch(IOException e) { log(e); }
    }

    private void csvToDatabase(String file) {
        csvToDatabase(new File(file));
    }

    private void test(String file) {
        if (!canRead(new File(file))) {
            log("can't handle " + file);
            return;
        }
        int count=0;
        try {
            BufferedReader br = new BufferedReader(new java.io.FileReader(file));
            String line = br.readLine();
            while (line != null) {
               String[] data = line.split(",");
               for (String item: data) { 
                   System.out.print(item + "\t"); 
               }
            System.out.println(); 
            line = br.readLine();
            count++;
            if (count>3)
                break; //its a test.
            }
            br.close();

            count=0;
            Reader in = new java.io.FileReader(file);
            CSVParser parser = new CSVParser(in, CSVFormat.EXCEL);
            //Reader in = new StringReader("a;b\nc;d");
            //CSVParser parser = new CSVParser(in, CSVFormat.EXCEL);
            List<CSVRecord> list = parser.getRecords();
            for(CSVRecord record : list) {
                Iterator<String> it = record.iterator();
                log("size " + record.size()); 
                if (count++>5)
                    break; //its a test
            }
        } catch(IOException e) { log(e); }
    }

    private static final Logger logger =
                         Logger.getLogger(CSVScanner.class.getName());

    private void log(String msg) {
        logger.info(msg);
    }

    private void log(Exception e ) {
        log(e.toString());
    }

    public static void main(String[] args) {
        CSVScanner myself = new CSVScanner();
        myself.create();
        if (args.length>1 && args[0].startsWith("-create")) {
            myself.makeTable();
            myself.csvToDatabase(args[1]);
        } else if (args.length>1 && args[0].startsWith("-test")) {
            myself.makeTable();
            myself.test(args[1]);
        } else if (args.length>0) {
            myself.csvToDatabase(args[0]);
        }
        myself.dispose();
    } 
} 

