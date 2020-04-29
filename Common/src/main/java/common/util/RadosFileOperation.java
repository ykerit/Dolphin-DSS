package common.util;

import com.ceph.rados.IoCTX;
import com.ceph.rados.exceptions.RadosException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;

public class RadosFileOperation {
    private static final Logger log = LogManager.getLogger(RadosFileOperation.class.getName());
    private final IoCTX ioCTX;

    public RadosFileOperation(IoCTX ioCTX) {
        this.ioCTX = ioCTX;
    }

    public String write(String path, long appID) {
        log.info("jar file: {}", path);
        File file = new File(path);
        String name = file.getName();
        // now use bio then will use aio
        try {
            ioCTX.write(file.getName() + "-" + appID, fileToByte(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return name;
    }

    public void read(String oid, String path, int length) throws RadosException {
        byte[] buffer = new byte[length];
        ioCTX.read(oid, length, 0, buffer);
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {
            File dir = new File(path);
            if (!dir.exists() && dir.isDirectory()) {
                dir.mkdirs();
            }
            file = new File(path + "/" + oid.substring(0, oid.indexOf('-')));
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(buffer);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private byte[] fileToByte(File file) throws IOException {
        byte[] buffer = null;
        if (file.exists()) {
            long size = file.length();
            FileInputStream inputStream = new FileInputStream(file);
            buffer = new byte[(int) size];
            int offset = 0;
            int readNum = 0;
            while (offset < buffer.length && (readNum = inputStream.read(buffer, offset, buffer.length - offset)) >= 0) {
                offset += readNum;
            }
            inputStream.close();
            if (offset != buffer.length) {
                throw new IOException("read error");
            }
        }
        return buffer;
    }
}
