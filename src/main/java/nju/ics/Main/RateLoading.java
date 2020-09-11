package nju.ics.Main;

import jdk.internal.util.xml.impl.Input;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class RateLoading {

    String version, file, filePath;
    int paramType;

    public String rateLoadingMethod(String jsonData) {

        JSONObject jsonObj = new JSONObject(jsonData);
        version = jsonObj.getString("version");
        file    = jsonObj.getString("file");
        filePath= jsonObj.getString("filePath");

        paramType = jsonObj.getInt("paramType");

        //TODO: read from zip file
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(filePath + File.separator + file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert zipFile != null;
        Enumeration<? extends ZipEntry> entries = zipFile.entries();

        while(entries.hasMoreElements()){
            ZipEntry entry = entries.nextElement();
            System.out.println(entry.getName());
            InputStream stream;
            try {
                stream = zipFile.getInputStream(entry);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return "";
    }
}
