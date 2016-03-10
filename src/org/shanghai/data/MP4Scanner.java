package org.shanghai.data;

import org.shanghai.data.FileTransporter.Delegate;
import org.shanghai.data.FileScanner;
import org.shanghai.util.ModelUtil;

import org.apache.jena.rdfxml.xmlinput.JenaReader;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.RDFReader;
import org.apache.jena.vocabulary.DCTerms;

import java.util.List;
import java.util.Map;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.IOException;
import java.io.FileInputStream;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.MovieBox;
import com.coremedia.iso.boxes.Box;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.coremedia.iso.boxes.UserDataBox;
import com.coremedia.iso.boxes.Container;
import com.coremedia.iso.boxes.MetaBox;
import com.coremedia.iso.boxes.MovieHeaderBox;

import java.util.logging.Logger;

/**
  @license http://www.apache.org/licenses/LICENSE-2.0
  @author Goetz Hatop
  @title A MP4 File Scanner 
  @date 2016-01-27
*/
public class MP4Scanner extends FileScanner implements Delegate {

    private boolean skip = true;
    private int count;
    private static final Logger logger =
                         Logger.getLogger(MP4Scanner.class.getName());

    public void log(String msg) {
        logger.info(msg);
    }


    public MP4Scanner() {}

    public MP4Scanner(boolean skip) {
        this.skip = skip;
    }

    @Override
    public Delegate create() {
        count = 0;
        return this;
    }

    @Override
    public void dispose() {
    }

    @Override
    public Resource read(String fname) {
        Resource rc = super.read(fname);
        try {
            this.scanFile(fname, rc);
        } //catch(FileNotFoundException e) { log(e); }
          catch(IOException e) { log(e); }
        finally {
            return rc;
        }
    }

    @Override
    public boolean canRead(String file) {
        String check = file.substring(0, file.lastIndexOf(".") + 1) + "mp4";
        if (skip && Files.isReadable(Paths.get(check))) {
            return false;
        } else {
            //log("check read [" + docbase + "] " + Paths.get(file));
            return Files.isReadable(Paths.get(file));
        }
    }

    private void scanFile(String fname, Resource rc) throws IOException {
        // may be later
        FileDataSourceImpl fd = new FileDataSourceImpl(fname);
        IsoFile isoFile = new IsoFile(fd);
        MovieBox moov = isoFile.getMovieBox();
        for(Box b : moov.getBoxes()) {
            System.out.println("box " + b);
        }
        MovieHeaderBox mbox = getOrNull(moov, MovieHeaderBox.class);
        System.out.println("mbox " + mbox);
        //UserDataBox userData = getOrNull(moov, UserDataBox.class);
        //System.out.println(userData);
        //MetaBox meta = getOrNull(userData, MetaBox.class);
        //System.out.println(meta);
    }

    private static <T extends Box> T getOrNull(Container box, Class<T> clazz) {
       if (box == null) return null;

       List<T> boxes = box.getBoxes(clazz);
       if (boxes.size() == 0) {
          return null;
       }
       return boxes.get(0);
    }

}
