/*
 * Copyright (C) 2018 Konstantinos
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2j.tools.dbinstaller;

import com.l2j.tools.dbinstaller.console.DBInstallerConsole;
import com.l2j.tools.dbinstaller.gui.DBConfigGUI;

import java.awt.HeadlessException;
import javax.swing.UIManager;

/**
 * @author Konstantinos
 */
public class LauncherLS extends AbstractDBLauncher{
    public static void main(String[] args){
        String defDatabase = "l2jloginserver";
        String dir = "../sql/login";
        String cleanUpScript = "ls_cleanup.sql";
        
        if((args != null) && (args.length >0)){
            new DBInstallerConsole(defDatabase, dir, cleanUpScript, getArg("-h", args), getArg("-p", args), getArg("-u", args), getArg("-pw", args), getArg("-d", args), getArg("-m", args));
            return;
        }
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch(Exception ex){
            
        }
        
        try{
            new DBConfigGUI(defDatabase, dir, cleanUpScript);
        }
        catch(HeadlessException ex){
            new DBInstallerConsole(defDatabase, dir, cleanUpScript);
        }
    }
}
