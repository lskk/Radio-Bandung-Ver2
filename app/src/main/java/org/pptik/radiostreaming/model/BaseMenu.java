package org.pptik.radiostreaming.model;

/**
 * Created by Hafid on 4/28/2017.
 */

public class BaseMenu {
    private int icon;
    private String menuText;

    public BaseMenu(int icon, String menuText) {
        this.icon = icon;
        this.menuText = menuText;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getMenuText() {
        return menuText;
    }

    public void setMenuText(String menuText) {
        this.menuText = menuText;
    }
}
