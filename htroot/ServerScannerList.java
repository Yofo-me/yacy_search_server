/**
 *  CrawlStartScanner_p
 *  Copyright 2010 by Michael Peter Christen, mc@yacy.net, Frankfurt am Main, Germany
 *  First released 12.08.2011 at http://yacy.net
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *  
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program in the file lgpl21.txt
 *  If not, see <http://www.gnu.org/licenses/>.
 */

import java.net.MalformedURLException;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;

import net.yacy.cora.document.ASCII;
import net.yacy.cora.protocol.RequestHeader;
import net.yacy.cora.protocol.Scanner;
import net.yacy.cora.protocol.Scanner.Access;
import net.yacy.kelondro.data.meta.DigestURI;
import net.yacy.kelondro.logging.Log;

import de.anomic.data.WorkTables;
import de.anomic.search.Switchboard;
import de.anomic.server.serverObjects;
import de.anomic.server.serverSwitch;

public class ServerScannerList {
    
    public static serverObjects respond(final RequestHeader header, final serverObjects post, final serverSwitch env) {
        
        final serverObjects prop = new serverObjects();
        final Switchboard sb = (Switchboard)env;

        prop.put("servertable_edit", post == null ? 0 : post.containsKey("edit") ? 1 : 0);
        prop.put("servertable_list_edit", post == null ? 0 : post.containsKey("edit") ? 1 : 0);
        prop.put("embedded", post == null ? 0 : post.containsKey("embedded") ? 1 : 0);
        prop.put("servertable", 0);
        
        // write scan table
        if (Scanner.scancacheSize() > 0) {
            // make a comment cache
            final Map<byte[], String> apiCommentCache = WorkTables.commentCache(sb);
            
            // show scancache table
            prop.put("servertable", 1);
            String urlString;
            DigestURI u;
            table: while (true) {
                try {
                    int i = 0;
                    final Iterator<Map.Entry<Scanner.Service, Scanner.Access>> se = Scanner.scancacheEntries();
                    Map.Entry<Scanner.Service, Scanner.Access> host;
                    while (se.hasNext()) {
                        host = se.next();
                        try {
                            u = new DigestURI(host.getKey().url());
                            urlString = u.toNormalform(true, false);
                            prop.put("servertable_list_" + i + "_edit_pk", ASCII.String(u.hash()));
                            prop.put("servertable_list_" + i + "_edit_count", i);
                            prop.putHTML("servertable_list_" + i + "_protocol", u.getProtocol());
                            prop.putHTML("servertable_list_" + i + "_ip", host.getKey().getInetAddress().getHostAddress());
                            prop.putHTML("servertable_list_" + i + "_url", urlString);
                            prop.put("servertable_list_" + i + "_accessUnknown", host.getValue() == Access.unknown ? 1 : 0);
                            prop.put("servertable_list_" + i + "_accessEmpty", host.getValue() == Access.empty ? 1 : 0);
                            prop.put("servertable_list_" + i + "_accessGranted", host.getValue() == Access.granted ? 1 : 0);
                            prop.put("servertable_list_" + i + "_accessDenied", host.getValue() == Access.denied ? 1 : 0);
                            prop.put("servertable_list_" + i + "_process", Scanner.inIndex(apiCommentCache, urlString) == null ? 0 : 1);
                            prop.put("servertable_list_" + i + "_edit_preselected", host.getValue() == Access.granted && Scanner.inIndex(apiCommentCache, urlString) == null ? 1 : 0);
                            i++;
                        } catch (MalformedURLException e) {
                            Log.logException(e);
                        }
                    }
                    prop.put("servertable_list", i);
                    prop.put("servertable_edit_num", i);
                    break table;
                } catch (ConcurrentModificationException e) {
                    continue table;
                }
            }
        }
        return prop;
    }
    
}
