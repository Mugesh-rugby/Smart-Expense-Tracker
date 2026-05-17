package model;

/**
 * Category - Entity class for expense/income categories.
 */
public class Category {

    public enum CategoryType { EXPENSE, INCOME, BOTH }

    private int          id;
    private String       name;
    private String       icon;
    private String       colorHex;
    private String       description;
    private CategoryType type;

    // ── Constructors ───────────────────────────────────────────────────────

    public Category() {}

    public Category(int id, String name, String icon, String colorHex,
                    String description, CategoryType type) {
        this.id          = id;
        this.name        = name;
        this.icon        = icon;
        this.colorHex    = colorHex;
        this.description = description;
        this.type        = type;
    }

    // ── Getters & Setters ──────────────────────────────────────────────────

    public int          getId()          { return id; }
    public void         setId(int id)    { this.id = id; }

    public String       getName()                { return name; }
    public void         setName(String name)     { this.name = name; }

    public String       getIcon()                { return icon; }
    public void         setIcon(String icon)     { this.icon = icon; }

    public String       getColorHex()                    { return colorHex; }
    public void         setColorHex(String colorHex)     { this.colorHex = colorHex; }

    public String       getDescription()                         { return description; }
    public void         setDescription(String description)       { this.description = description; }

    public CategoryType getType()                    { return type; }
    public void         setType(CategoryType type)   { this.type = type; }

    /** Display string used inside JComboBox. */
    @Override
    public String toString() { return icon + " " + name; }
}
