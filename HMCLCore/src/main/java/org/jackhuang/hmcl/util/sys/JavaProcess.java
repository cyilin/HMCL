/*
 * Hello Minecraft!.
 * Copyright (C) 2013  huangyuhui <huanghongxun2008@126.com>
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
 * along with this program.  If not, see {http://www.gnu.org/licenses/}.
 */
package org.jackhuang.hmcl.util.sys;

import java.util.ArrayList;
import org.jackhuang.hmcl.api.IProcess;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author huangyuhui
 */
public class JavaProcess implements IProcess {

    private final List<String> commands;
    private final Process process;
    private final List<String> stdOutLines = Collections.synchronizedList(new ArrayList<>());

    public JavaProcess(List<String> commands, Process process) {
        this.commands = commands;
        this.process = process;
    }

    public JavaProcess(String[] commands, Process process) {
        this(Arrays.asList(commands), process);
    }

    @Override
    public Process getRawProcess() {
        return this.process;
    }

    @Override
    public List<String> getStartupCommands() {
        return this.commands;
    }

    @Override
    public String getStartupCommand() {
        return this.process.toString();
    }

    @Override
    public List<String> getStdOutLines() {
        return this.stdOutLines;
    }

    @Override
    public boolean isRunning() {
        try {
            this.process.exitValue();
        } catch (IllegalThreadStateException ex) {
            return true;
        }

        return false;
    }

    @Override
    public int getExitCode() {
        try {
            return this.process.exitValue();
        } catch (IllegalThreadStateException ex) {
            ex.fillInStackTrace();
            throw ex;
        }
    }

    @Override
    public String toString() {
        return "JavaProcess[commands=" + this.commands + ", isRunning=" + isRunning() + "]";
    }

    @Override
    public void stop() {
        this.process.destroy();
    }
}
