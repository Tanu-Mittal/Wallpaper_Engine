import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class MainWindow extends JFrame {

    // --- DATA ---
    private File assetsDir;
    private boolean isDarkMode = false; 

    // --- UI COMPONENTS ---
    private JPanel gridPanel;
    private JLabel statusLabel;
    private JPanel sidebar;
    private JPanel mainContent;
    private JScrollPane scrollPane;
    private JButton btnThemeToggle;
    private JLabel titleLabel; 
    
    // --- THEME COLORS ---
    private Color BG_MAIN, BG_SIDEBAR, CARD_BG, TEXT_PRIMARY, BORDER_COLOR;
    private final Color ACCENT_BLUE = new Color(0, 120, 215);

    public MainWindow() {
        // 1. SETUP PATH
        assetsDir = new File("assets");
        if (!assetsDir.exists()) assetsDir = new File("C:\\Users\\tanum\\OneDrive\\Desktop\\DynamicWallpaperEngine\\assets");
        if (!assetsDir.exists()) assetsDir.mkdirs();

        applyThemeColors(); 
        setupUI();
        
        // 2. INITIAL LOAD
        loadLibrary("ALL"); 
    }

    private void applyThemeColors() {
        if (isDarkMode) {
            BG_MAIN = new Color(30, 30, 30);
            BG_SIDEBAR = new Color(40, 40, 40);
            CARD_BG = new Color(50, 50, 50);
            TEXT_PRIMARY = new Color(240, 240, 240);
            BORDER_COLOR = new Color(70, 70, 70);
        } else {
            BG_MAIN = new Color(245, 247, 250);
            BG_SIDEBAR = new Color(255, 255, 255);
            CARD_BG = new Color(255, 255, 255);
            TEXT_PRIMARY = new Color(33, 37, 41);
            BORDER_COLOR = new Color(220, 220, 220);
        }
    }

    private void toggleTheme() {
        isDarkMode = !isDarkMode;
        applyThemeColors();
        
        sidebar.setBackground(BG_SIDEBAR);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));
        mainContent.setBackground(BG_MAIN);
        gridPanel.setBackground(BG_MAIN);
        scrollPane.setBackground(BG_MAIN);
        titleLabel.setForeground(TEXT_PRIMARY);
        
        loadLibrary("ALL"); 
        btnThemeToggle.setText(isDarkMode ? "☀ Light Mode" : "🌙 Dark Mode");
    }

    private void setupUI() {
        setTitle("Wallpaper Engine");
        setSize(1200, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- SIDEBAR ---
        sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));

        JLabel brand = new JLabel("Wallpaper Engine");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 18));
        brand.setForeground(ACCENT_BLUE);
        brand.setBorder(new EmptyBorder(20, 20, 30, 20));
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(brand);

        // --- NAVIGATION BUTTONS ---
        sidebar.add(createFilterButton("All Wallpapers", "ALL"));
        sidebar.add(createFilterButton("Images Only", "IMG"));
        sidebar.add(createFilterButton("Videos Only", "VID"));
        sidebar.add(createFilterButton("GIFs Only", "GIF")); // NEW BUTTON
        
        sidebar.add(Box.createVerticalGlue()); 

        btnThemeToggle = new JButton("🌙 Dark Mode");
        styleButton(btnThemeToggle);
        btnThemeToggle.addActionListener(e -> toggleTheme());
        sidebar.add(btnThemeToggle);
        sidebar.add(Box.createVerticalStrut(10));

        JButton btnOpenFolder = new JButton("📂 Open Folder");
        styleButton(btnOpenFolder);
        btnOpenFolder.addActionListener(e -> openFolder());
        sidebar.add(btnOpenFolder);
        sidebar.add(Box.createVerticalStrut(20));

        add(sidebar, BorderLayout.WEST);

        // --- MAIN CONTENT ---
        mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(BG_MAIN);

        JPanel headerBar = new JPanel(new BorderLayout());
        headerBar.setOpaque(false);
        headerBar.setBorder(new EmptyBorder(20, 30, 10, 30));
        
        titleLabel = new JLabel("Library");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_PRIMARY);
        statusLabel = new JLabel("Ready");
        statusLabel.setForeground(Color.GRAY);

        headerBar.add(titleLabel, BorderLayout.WEST);
        headerBar.add(statusLabel, BorderLayout.EAST);
        mainContent.add(headerBar, BorderLayout.NORTH);

        gridPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 20, 20));
        gridPanel.setBackground(BG_MAIN);
        gridPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        scrollPane = new JScrollPane(gridPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainContent.add(scrollPane, BorderLayout.CENTER);

        add(mainContent, BorderLayout.CENTER);
    }

    private void styleButton(JButton btn) {
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(180, 40));
        btn.setBackground(new Color(230, 230, 230));
        btn.setFocusPainted(false);
    }

    private JButton createFilterButton(String text, String filterType) {
        JButton btn = new JButton(text);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(220, 40));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setForeground(ACCENT_BLUE);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> loadLibrary(filterType));
        return btn;
    }

    // --- LIBRARY LOADING LOGIC ---
    private void loadLibrary(String filter) {
        System.out.println("Filter: " + filter);
        gridPanel.removeAll();

        if (assetsDir.exists()) {
            File[] files = assetsDir.listFiles();
            if (files != null) Arrays.sort(files);

            if (files != null) {
                for (File f : files) {
                    String n = f.getName().toLowerCase();
                    
                    boolean isMp4 = n.endsWith(".mp4") || n.endsWith(".avi");
                    boolean isGif = n.endsWith(".gif");
                    boolean isStatic = n.endsWith(".jpg") || n.endsWith(".png") || n.endsWith(".jpeg");

                    if (!isMp4 && !isGif && !isStatic) continue; 

                    // PREVENT DUPLICATES (Hide GIF if it is a video preview)
                    if (isGif) {
                        File matchingVideo = new File(assetsDir, f.getName().replace(".gif", ".mp4"));
                        if (matchingVideo.exists()) continue; 
                    }

                    // APPLY FILTER
                    boolean show = false;
                    if (filter.equals("ALL")) show = true;
                    else if (filter.equals("VID") && isMp4) show = true;
                    else if (filter.equals("GIF") && isGif) show = true; // GIF Only
                    else if (filter.equals("IMG") && isStatic) show = true; // Static Only

                    if (show) addCard(f);
                }
            }
        }
        
        gridPanel.revalidate();
        gridPanel.repaint();
        checkPreviews();
    }

    private void addCard(File f) {
        JPanel card = new JPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(240, 220)); 
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));

        JLabel thumb = new JLabel();
        thumb.setHorizontalAlignment(SwingConstants.CENTER);
        thumb.setBackground(isDarkMode ? new Color(60,60,60) : new Color(240,240,240));
        thumb.setOpaque(true);
        thumb.setPreferredSize(new Dimension(240, 160)); 
        
        ImageIcon icon = getThumbnail(f);
        if (icon != null) {
            thumb.setIcon(icon);
            thumb.setText("");
        } else {
            thumb.setText("Generating...");
        }

        JPanel info = new JPanel(new BorderLayout());
        info.setBackground(CARD_BG);
        info.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        JLabel name = new JLabel(f.getName());
        name.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        name.setForeground(TEXT_PRIMARY);
        
        info.add(name, BorderLayout.CENTER);
        card.add(thumb, BorderLayout.CENTER);
        card.add(info, BorderLayout.SOUTH);

        thumb.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { applyWallpaper(f); }
            public void mouseEntered(MouseEvent e) { card.setBorder(BorderFactory.createLineBorder(ACCENT_BLUE, 2)); }
            public void mouseExited(MouseEvent e) { card.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1)); }
        });

        gridPanel.add(card);
    }

    private ImageIcon getThumbnail(File f) {
        String name = f.getName().toLowerCase();
        String path = f.getAbsolutePath();
        try {
            if (name.endsWith(".mp4") || name.endsWith(".avi")) {
                String base = path.substring(0, path.lastIndexOf("."));
                File gif = new File(base + ".gif");
                if (gif.exists()) return new ImageIcon(gif.getAbsolutePath());
                return null; 
            } else if (name.endsWith(".gif")) {
                return new ImageIcon(path); // No scale for GIFs
            } else {
                ImageIcon icon = new ImageIcon(path);
                Image img = icon.getImage();
                if (img.getWidth(null) > 0) {
                    Image scaled = img.getScaledInstance(240, 160, Image.SCALE_SMOOTH);
                    return new ImageIcon(scaled);
                }
                return null;
            }
        } catch (Exception e) { return null; }
    }

    private void checkPreviews() {
        new Thread(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder("python", "../fix_previews.py", assetsDir.getAbsolutePath());
                pb.start().waitFor();
            } catch (Exception e) {}
        }).start();
    }

    private void applyWallpaper(File f) {
        String name = f.getName().toLowerCase();
        String script = name.endsWith(".mp4") ? "video_wallpaper.py" : "set_static_wallpaper.py";
        if(name.endsWith(".gif")) script = "animated_wallpaper.py";
        
        statusLabel.setText("Applying: " + f.getName());
        try {
            new ProcessBuilder("python", "../python_scripts/" + script, f.getAbsolutePath()).start();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void openFolder() {
        JFileChooser c = new JFileChooser();
        c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (c.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            assetsDir = c.getSelectedFile();
            loadLibrary("ALL");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainWindow().setVisible(true));
    }
}

class WrapLayout extends FlowLayout {
    public WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }
    @Override public Dimension preferredLayoutSize(Container target) { return layoutSize(target, true); }
    @Override public Dimension minimumLayoutSize(Container target) { return layoutSize(target, false); }
    private Dimension layoutSize(Container target, boolean preferred) {
        synchronized (target.getTreeLock()) {
            int targetWidth = target.getSize().width;
            if (targetWidth == 0) targetWidth = Integer.MAX_VALUE;
            int hgap = getHgap(), vgap = getVgap();
            Insets insets = target.getInsets();
            int maxWidth = targetWidth - (insets.left + insets.right + (hgap * 2));
            Dimension dim = new Dimension(0, 0);
            int rowWidth = 0, rowHeight = 0;
            for (int i = 0; i < target.getComponentCount(); i++) {
                Component m = target.getComponent(i);
                if (m.isVisible()) {
                    Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                    if (rowWidth + d.width > maxWidth) {
                        dim.width = Math.max(dim.width, rowWidth);
                        dim.height += rowHeight + vgap;
                        rowWidth = 0; rowHeight = 0;
                    }
                    rowWidth += d.width + hgap;
                    rowHeight = Math.max(rowHeight, d.height);
                }
            }
            dim.width = Math.max(dim.width, rowWidth);
            dim.height += rowHeight + vgap + insets.top + insets.bottom;
            return dim;
        }
    }
}
