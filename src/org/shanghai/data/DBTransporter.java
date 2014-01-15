package org.shanghai.data;

import org.shanghai.crawl.MetaCrawl;
import org.shanghai.util.FileUtil;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;

import java.io.InputStream;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
    @license http://www.apache.org/licenses/LICENSE-2.0
    @author Alfred Steinacker
    @title Simple Database Transporter
    @date 2013-11-16
*/
public class DBTransporter implements MetaCrawl.Transporter {

    protected Database database;
    protected String table;
    protected String iri;
    private String idxQuery;
    private String dumpQuery;

    private DBTransporter() {
        database = new Database();
    }

    public DBTransporter(String host, String db, String user, String pass) {
        database = new Database(host, db, user, pass);
    }

    public DBTransporter(String host, String db, String user, String pass
                         ,String idxFile, String dumpFile, String iri) {
        this(host, db, user, pass);
        idxQuery = FileUtil.read(idxFile);
        dumpQuery = FileUtil.read(dumpFile);
        this.iri = iri;
    }

    public DBTransporter(Database db, String idxQuery, String dumpQuery,
                         String iri) {
        this.database = db;
        this.idxQuery = idxQuery;
        this.dumpQuery = dumpQuery;
        this.iri = iri;
    }

    @Override
    public void create() {
        database.create();
        int x = idxQuery.indexOf("from")+5;
        int y = idxQuery.indexOf(" ",x);
        y=y>0?y:idxQuery.length();
        if (x>0 && y>x)
        table = idxQuery.substring(x,y).trim();
        log("main guessed table is [" + table + "]");
    }

    @Override
    public void dispose() {
        database.dispose();
    }

    @Override
    public String probe() {
        String probe = database.getSingleText("show tables");
        //log("probe: " + probe);
        return probe;
    }

    @Override
    public Model read(String oid) {
        //log("read : " + oid);
        Model model = ModelFactory.createDefaultModel();
        String query = dumpQuery.replace("<oid>",oid);
        ResultSet rs = database.getSingleRow(query);
                  // "select * from " + table + " where oid='" + oid + "'");
        if (rs==null)
            return model;
        try {
            ResultSetMetaData meta = rs.getMetaData();
            model.setNsPrefix(table, iri);
            rs.next(); //go to first row
            //Resource rConcept = model.createResource(concept);
            Resource resource = model.createResource(iri + oid);
            for (int col=1; col<=meta.getColumnCount(); col++) {
                Property p = model.createProperty(iri,meta.getColumnName(col));
                switch (meta.getColumnType(col)) {
                    //maybe later : add type knowledge somehow
                    case Types.VARCHAR:
                        resource.addProperty(p,rs.getString(col));
                        break;
                    default: 
                        resource.addProperty(p,rs.getString(col));
                        break;
                }
            }
        } catch(SQLException e) { log(e); }
        return model;
    }

    @Override
    public String getIdentifier(String name) {
        return name;
    }

    @Override
    public String[] getIdentifiers(int off, int limit) {
        log("identifiers : " + off + " " + limit);
        //String query = "select oid from " 
        //             + table + " limit " + off +"," + limit;    
        String query = idxQuery + " limit " + off +"," + limit;
        return database.getColumn(query,1,limit);
    }

    @Override
    public int crawl(String str) {
        //this.table = table;
        //if (idxQuery==null)
        //    idxQuery = "select oid from " + table;
        //if (dumpQuery==null)
        //    dumpQuery = "select * from " + table + " where oid=<oid>";
        int result = database.getSingleInt("select count(*) from " + table);
        return result;
    }

    private static final Logger logger =
                         Logger.getLogger(DBTransporter.class.getName());

    private void log(String msg) {
        logger.info(msg);
    }

    private void log(Exception ex) {
        logger.info(ex.toString());
        ex.printStackTrace();
    }
}
