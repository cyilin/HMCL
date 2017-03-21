/*
 * Copyright (C) 2015 Jack Jiang(cngeeker.com) The BeautyEye Project. 
 * All rights reserved.
 * Project URL:https://github.com/JackJiang2011/beautyeye
 * Version 3.6
 * 
 * Jack Jiang PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * BETextFieldUI.java at 2015-2-1 20:25:37, original version by Jack Jiang.
 * You can contact author with jb2011@163.com.
 */
package org.jackhuang.hmcl.laf.textcoms;

import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.text.JTextComponent;

import org.jackhuang.hmcl.laf.textcoms.__UI__.BgSwitchable;
import org.jackhuang.hmcl.laf.utils.TMSchema;

/**
 * 文本组件JTextField的UI实现类.
 *
 * @author Jack Jiang(jb2011@163.com)
 */
public class BETextFieldUI extends BasicTextFieldUI implements BgSwitchable,
        org.jackhuang.hmcl.laf.BeautyEyeLNFHelper.__UseParentPaintSurported {

    public static BETextFieldUI createUI(JComponent c) {
        addOtherListener(c);
        return new BETextFieldUI();
    }

    //* 本方法由Jack Jiang于2012-09-07日加入
    /**
     * 是否使用父类的绘制实现方法，true表示是.
     * <p>
     * 因为在BE LNF中，边框和背景等都是使用N9图，没法通过设置背景色和前景
     * 色来控制JTextField的颜色和边框，本方法的目的就是当用户设置了进度条的border或背景色 时告之本实现类不使用BE
     * LNF中默认的N9图填充绘制而改用父类中的方法（父类中的方法 就可以支持颜色的设置罗，只是丑点，但总归是能适应用户的需求场景要求，其实用户完全可以
     * 通过JTextField.setUI(..)方式来自定义UI哦）.
     *
     * @return true, if is use parent paint
     */
    @Override
    public boolean isUseParentPaint() {
        return getComponent() != null
                && (!(getComponent().getBorder() instanceof UIResource)
                || !(getComponent().getBackground() instanceof UIResource));
    }

    /**
     * Paints a background for the view. This will only be called if isOpaque()
     * on the associated component is true. The default is to paint the
     * background color of the component.
     *
     * @param g the graphics context
     */
    @Override
    protected void paintBackground(Graphics g) {
        //先调用父类方法把背景刷新下（比如本UI里使用的大圆角NP图如不先刷新背景则会因上下拉动滚动条
        //而致4个圆角位置得不到刷新，从而影响视觉效果（边角有前面的遗留），置于透明边角不被透明像素填
        //充的问题，它有可能是Android的NinePatch技术为了性能做作出的优化——一切全透明像素即意味着不需绘制）
        super.paintBackground(g);// TODO 出于节约计算资源考生虑，本行代码换成父类中默认填充背景的代码即可

        //* 如果用户作了自定义颜色设置则使用父类方法来实现绘制，否则BE LNF中没法支持这些设置哦
        if (!isUseParentPaint()) {
            //用新的NP图实现真正的背景填充
            JTextComponent editor = this.getComponent();
            __UI__.TextSkin.paintBg(editor, g, 0, 0, editor.getWidth(), editor.getHeight(), state);
        }
    }

    @Override
    public void switchBgToNormal() {
        state = TMSchema.State.NORMAL;
    }

    @Override
    public void switchBgToFocused() {
        state = TMSchema.State.FOCUSED;
    }

    @Override
    public void switchBgToOver() {
        state = TMSchema.State.ROLLOVER;
    }

    TMSchema.State state = TMSchema.State.NORMAL;

    /**
     * 为组件添加焦点监听器（获得/取消焦点时可以自动设置/取消一个彩色的边框效果，以体高UI体验） 、右键菜单监听器（有复制/粘贴等功能）.
     *
     * @param c the c
     */
    public static void addOtherListener(JComponent c) {
        c.addFocusListener(FocusListenerImpl.getInstance());
        c.addMouseListener(FocusListenerImpl.getInstance());
    }

}
