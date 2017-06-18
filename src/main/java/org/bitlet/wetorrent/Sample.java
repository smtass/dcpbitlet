/*
 *              bitlet - Simple bittorrent library
 *  Copyright (C) 2008 Alessandro Bahgat Shehata, Daniele Castagna
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.bitlet.wetorrent;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import org.bitlet.wetorrent.disk.PlainFileSystemTorrentDisk;
import org.bitlet.wetorrent.disk.TorrentDisk;
import org.bitlet.wetorrent.peer.IncomingPeerListener;
import org.bitlet.wetorrent.util.Utils;

import static org.bitlet.wetorrent.util.Utils.byteToHumanReadableString;
import static org.bitlet.wetorrent.util.Utils.toByteBuffer;

public class Sample {
    private static final int PORT = 6881;

    public static void main(String[] args) throws Exception {
        // read torrent filename from command line arg
        String filename = args[0];

        // Parse the metafile
        Metafile metafile = new Metafile(new BufferedInputStream(new FileInputStream(filename)));
        // Create the torrent disk, this is the destination where the torrent file/s will be saved
        printKeyInfo(metafile);

        TorrentDisk tdisk = new PlainFileSystemTorrentDisk(metafile, new File("."));
        tdisk.init();
        
        IncomingPeerListener peerListener = new IncomingPeerListener(PORT);
        peerListener.start();

        Torrent torrent = new Torrent(metafile, tdisk, peerListener);
        torrent.startDownload();

        while (!torrent.isCompleted()) {

            try {
                Thread.sleep(1000);
            } catch(InterruptedException ie) {
                break;
            }

            torrent.tick();
            System.out.printf("Got %s peers, completed %d bytes\n",
                    torrent.getPeersManager().getActivePeersNumber(),
                    torrent.getTorrentDisk().getCompleted());
        }

        torrent.interrupt();
        peerListener.interrupt();
    }

    protected static void printKeyInfo(Metafile metafile) throws IOException{
        //String encoding = System.getProperty("file.encoding");
        //System.out.println(encoding);
        /*
        System.out.write("奇异博士".getBytes());
        System.out.println();
        System.out.print(Integer.toHexString("奇异博士".getBytes()[0]& 0xFF) );
        System.out.print(Integer.toHexString("奇异博士".getBytes()[1]& 0xFF) );
        System.out.println(Integer.toHexString("奇异博士".getBytes()[2]& 0xFF) );

        System.out.print(Integer.toHexString(metafile.getName().getBytes()[0]& 0xFF));
        System.out.print(Integer.toHexString(metafile.getName().getBytes()[1]& 0xFF));
        System.out.println(Integer.toHexString(metafile.getName().getBytes()[2]& 0xFF));
        //System.exit(0);

        String unicode = new String(metafile.getName().getBytes(),"UTF-8");
        String gbk = new String(unicode.getBytes("GBK"));

        System.out.print(Integer.toHexString(unicode.getBytes("GBK")[0] & 0xFF) );
        System.out.print(Integer.toHexString(unicode.getBytes("GBK")[1] & 0xFF) );
        System.out.println(Integer.toHexString(unicode.getBytes("GBK")[2] & 0xFF ));

        System.out.print(gbk.getBytes()[0] & 0xFF );
        System.out.print(gbk.getBytes()[1] & 0xFF );
        System.out.println(gbk.getBytes()[2] & 0xFF );
        System.exit(0);
        */
        /*
        byte [] barr = metafile.getName().getBytes();
        for(int tmp = 0; tmp < barr.length; tmp++){
            System.out.print(Integer.toHexString(barr[tmp] & 0xFF));
        }
        System.out.println();
        barr = Utils.utf8ToLocalCharset(metafile.getName());
        for(int tmp = 0; tmp < barr.length; tmp++){
            System.out.print(Integer.toHexString(barr[tmp] & 0xFF));
        }
        System.out.println();
        */
        System.out.write(Utils.utf8ToLocalCharset(metafile.getName()));
        System.out.println();
        System.out.println(metafile.getAnnounce());
        List al = metafile.getAnnounceList();
        for (int i = 0; i < al.size(); i++) {
            ByteBuffer bb = (ByteBuffer)(((List)(al.get(i))).get(0));
            System.out.write(Utils.utf8ToLocalCharset(bb.array()));
            System.out.println();
        }
        List files = metafile.getFiles();
        for (int i = 0; i < files.size(); i++) {
            Map fm = (Map)files.get(i);
            List pl = (List)fm.get(toByteBuffer("path"));
            for(int ip = 0; ip < pl.size(); ip++) {
                ByteBuffer bb = (ByteBuffer) (pl.get(ip));
                byte[] byteString = bb.array();
                System.out.write(Utils.utf8ToLocalCharset(byteString));
            }
            System.out.write(' ');
            Long len = (Long) fm.get(toByteBuffer("length"));
            System.out.println(byteToHumanReadableString(len));
        }
        //System.exit(0);
    }


}
