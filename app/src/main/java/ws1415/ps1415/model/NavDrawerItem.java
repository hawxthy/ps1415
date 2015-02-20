package ws1415.ps1415.model;

/**
 * Model-Klasse für die Elemente in dem Navigation Drawer.
 *
 * @author Martin Wrodarczyk
 */
public class NavDrawerItem {
    private String title;
    private int icon;

    public NavDrawerItem(){}

    /**
     * @param title Titel
     * @param icon ResId des Icons
     */
    public NavDrawerItem(String title, int icon){
        this.title = title;
        this.icon = icon;
    }

    public String getTitle(){
        return this.title;
    }

    public int getIcon(){
        return this.icon;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setIcon(int icon){
        this.icon = icon;
    }
}