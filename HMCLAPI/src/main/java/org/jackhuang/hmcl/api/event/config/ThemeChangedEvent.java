/*
 * Hello Minecraft! Launcher.
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
package org.jackhuang.hmcl.api.event.config;

import org.jackhuang.hmcl.api.event.SimpleEvent;
import org.jackhuang.hmcl.api.ui.Theme;

/**
 * This event gets fired when the application theme changed.
 * <br>
 * This event is fired on the {@link org.jackhuang.hmcl.api.HMCLApi#EVENT_BUS}
 * @param source {@link org.jackhuang.hmcl.setting.Config}
 * @param Theme the changed theme
 * @author huangyuhui
 */
public class ThemeChangedEvent extends SimpleEvent<Theme> {
    
    public ThemeChangedEvent(Object source, Theme value) {
        super(source, value);
    }
    
}
