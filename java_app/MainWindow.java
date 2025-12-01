import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Arrays;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.border.*;

public class MainWindow extends JFrame {

    private File assetsDir;
    private String currentTheme = "LIGHT";
    private String currentFilter = "ALL";
    private Preferences prefs;

    private JPanel gridPanel, sidebar, mainContent;
    private JScrollPane scrollPane;
    private JLabel titleLabel, statusLabel, brandLabel;
    private JButton btnRefresh, btnFolder;
    
    // Dynamic Theme Colors
    private Color BG_MAIN, BG_SIDEBAR, CARD_BG, TEXT_PRIMARY, TEXT_SECONDARY, BORDER_COLOR, ACCENT, NAV_HOVER;

    public MainWindow() {
        // Load saved folder path
        prefs = Preferences.userNodeForPackage(MainWindow.class);
        String savedPath = prefs.get("assetsFolder", "assets");
        
        assetsDir = new File(savedPath);
        if (!assetsDir.exists()) assetsDir = new File("assets");
        if (!assetsDir.exists()) {
            assetsDir = new File("C:\\Users\\tanum\\OneDrive\\Desktop\\DynamicWallpaperEngine\\assets");
        }
        if (!assetsDir.exists()) assetsDir.mkdirs();

        applyTheme();
        setupUI();
        loadLibrary("ALL");
    }

    private void applyTheme() {
        switch (currentTheme) {
            case "DARK":
                BG_MAIN = new Color(25, 25, 25);
                BG_SIDEBAR = new Color(35, 35, 35);
                CARD_BG = new Color(45, 45, 45);
                TEXT_PRIMARY = new Color(240, 240, 240);
                TEXT_SECONDARY = new Color(180, 180, 180);
                BORDER_COLOR = new Color(60, 60, 60);
                ACCENT = new Color(0, 150, 255);
                NAV_HOVER = new Color(55, 55, 55);
                break;
            case "RETRO":
                BG_MAIN = new Color(20, 10, 40);
                BG_SIDEBAR = new Color(40, 20, 60);
                CARD_BG = new Color(60, 30, 80);
                TEXT_PRIMARY = new Color(255, 200, 255);
                TEXT_SECONDARY = new Color(200, 150, 255);
                BORDER_COLOR = new Color(150, 50, 200);
                ACCENT = new Color(255, 0, 150);
                NAV_HOVER = new Color(70, 40, 90);
                break;
            default: // LIGHT
                BG_MAIN = new Color(248, 249, 250);
                BG_SIDEBAR = new Color(255, 255, 255);
                CARD_BG = new Color(255, 255, 255);
                TEXT_PRIMARY = new Color(33, 37, 41);
                TEXT_SECONDARY = new Color(108, 117, 125);
                BORDER_COLOR = new Color(230, 230, 230);
                ACCENT = new Color(0, 120, 215);
                NAV_HOVER = new Color(245, 245, 245);
        }
    }

    private void switchTheme(String theme) {
        currentTheme = theme;
        applyTheme();
        refreshUI();
        
        // Update button colors immediately
        if (btnRefresh != null) {
            btnRefresh.setBackground(ACCENT);
            btnRefresh.repaint();
        }
        if (btnFolder != null) {
            btnFolder.setBackground(ACCENT);
            btnFolder.repaint();
        }
        
        loadLibrary(currentFilter);
    }

    private void refreshUI() {
        sidebar.setBackground(BG_SIDEBAR);
        mainContent.setBackground(BG_MAIN);
        gridPanel.setBackground(BG_MAIN);
        scrollPane.setBackground(BG_MAIN);
        scrollPane.getViewport().setBackground(BG_MAIN);
        
        titleLabel.setForeground(TEXT_PRIMARY);
        statusLabel.setForeground(TEXT_SECONDARY);
        brandLabel.setForeground(ACCENT);
        
        sidebar.removeAll();
        buildSidebar();
        sidebar.revalidate();
        sidebar.repaint();
    }

    private void setupUI() {
        setTitle("Dynamic Windows Wallpaper");
        
        // Get screen bounds (excludes taskbar automatically)
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Rectangle screenBounds = ge.getMaximumWindowBounds();
        
        // Use 85% of available screen space
        int windowWidth = (int)(screenBounds.width * 0.85);
        int windowHeight = (int)(screenBounds.height * 0.85);
        
        setSize(windowWidth, windowHeight);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Enable smoother rendering
        System.setProperty("awt.useSystemAAFontSettings","on");
        System.setProperty("swing.aatext", "true");

        // === SIDEBAR ===
        sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(250, 0));
        
        buildSidebar();
        add(sidebar, BorderLayout.WEST);

        // === MAIN CONTENT ===
        mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(BG_MAIN);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(30, 40, 20, 40));

        titleLabel = new JLabel("Library");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(TEXT_PRIMARY);

        // Right side buttons
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightPanel.setOpaque(false);

        btnRefresh = createHeaderButton("Refresh");
        btnRefresh.addActionListener(e -> {
            statusLabel.setText("Refreshing...");
            loadLibrary(currentFilter);
            Timer timer = new Timer(1000, evt -> statusLabel.setText("Ready"));
            timer.setRepeats(false);
            timer.start();
        });

        btnFolder = createHeaderButton("Browse");
        btnFolder.addActionListener(e -> openNativeFileDialog());

        rightPanel.add(btnRefresh);
        rightPanel.add(btnFolder);

        header.add(titleLabel, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);
        mainContent.add(header, BorderLayout.NORTH);

        // Grid
        gridPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 25, 25));
        gridPanel.setBackground(BG_MAIN);
        gridPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        scrollPane = new JScrollPane(gridPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        scrollPane.getViewport().setBackground(BG_MAIN);
        
        // Smooth scrolling with custom scrollbar
        scrollPane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = BORDER_COLOR;
                this.trackColor = BG_MAIN;
            }
            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }
            @Override    
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }
            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                return button;
            }
        });
        
        mainContent.add(scrollPane, BorderLayout.CENTER);
        add(mainContent, BorderLayout.CENTER);
        
        // Clean up wallpapers on close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                killAllWallpapers();
            }
        });
    }

    private JButton createHeaderButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Rounded rectangle background
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
                // Text
                g2.setColor(getForeground());
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(getText(), x, y);
                
                g2.dispose();
            }
        };
        
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(ACCENT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(100, 38));
        
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { 
                btn.setBackground(new Color(
                    Math.min(255, ACCENT.getRed() + 30),
                    Math.min(255, ACCENT.getGreen() + 30),
                    Math.min(255, ACCENT.getBlue() + 30)
                ));
                btn.repaint();
            }
            public void mouseExited(MouseEvent e) { 
                btn.setBackground(ACCENT);
                btn.repaint();
            }
        });
        
        return btn;
    }

    private void buildSidebar() {
        brandLabel = new JLabel("<html><b>Wallpaper</b><br>Engine</html>");
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        brandLabel.setForeground(ACCENT);
        brandLabel.setBorder(new EmptyBorder(35, 30, 45, 30));
        brandLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(brandLabel);

        sidebar.add(createSectionLabel("LIBRARY"));
        sidebar.add(createNavButton("All Wallpapers", "ALL"));
        sidebar.add(createNavButton("Images Only", "IMG"));
        sidebar.add(createNavButton("Videos Only", "VID"));
        sidebar.add(createNavButton("GIFs Only", "GIF"));
        
        sidebar.add(Box.createVerticalStrut(15));
        sidebar.add(createSeparator());
        sidebar.add(Box.createVerticalStrut(15));
        
        sidebar.add(createSectionLabel("THEMES"));
        sidebar.add(createThemeButton("Light", "LIGHT"));
        sidebar.add(createThemeButton("Dark", "DARK"));
        sidebar.add(createThemeButton("Retro", "RETRO"));
        
        sidebar.add(Box.createVerticalGlue());
        
        sidebar.add(Box.createVerticalStrut(20));
        sidebar.add(createSeparator());
        sidebar.add(Box.createVerticalStrut(12));
        
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        statusLabel.setForeground(TEXT_SECONDARY);
        statusLabel.setBorder(new EmptyBorder(5, 30, 25, 30));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(statusLabel);
    }

    private JSeparator createSeparator() {
        JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
        sep.setMaximumSize(new Dimension(210, 1));
        sep.setForeground(BORDER_COLOR);
        sep.setBackground(BORDER_COLOR);
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        return sep;
    }

    private JLabel createSectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(TEXT_SECONDARY);
        lbl.setBorder(new EmptyBorder(8, 30, 10, 30));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JButton createNavButton(String text, String filter) {
        JButton btn = new JButton(text);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(250, 42));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setForeground(TEXT_PRIMARY);
        btn.setBackground(BG_SIDEBAR);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(11, 30, 11, 30));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { 
                btn.setBackground(NAV_HOVER); 
                btn.setContentAreaFilled(true); 
            }
            public void mouseExited(MouseEvent e) { 
                btn.setContentAreaFilled(false); 
            }
        });
        btn.addActionListener(e -> {
            currentFilter = filter;
            loadLibrary(filter);
        });
        return btn;
    }

    private JButton createThemeButton(String text, String theme) {
        JButton btn = new JButton(text);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(250, 42));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setForeground(TEXT_PRIMARY);
        btn.setBackground(BG_SIDEBAR);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(11, 30, 11, 30));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { 
                btn.setBackground(NAV_HOVER); 
                btn.setContentAreaFilled(true); 
            }
            public void mouseExited(MouseEvent e) { 
                btn.setContentAreaFilled(false); 
            }
        });
        btn.addActionListener(e -> switchTheme(theme));
        return btn;
    }

    private void loadLibrary(String filter) {
        gridPanel.removeAll();
        
        if (assetsDir.exists()) {
            File[] files = assetsDir.listFiles();
            if (files != null) {
                Arrays.sort(files);
                
                for (File f : files) {
                    String n = f.getName().toLowerCase();
                    
                    boolean isMp4 = n.endsWith(".mp4") || n.endsWith(".avi");
                    boolean isGif = n.endsWith(".gif");
                    boolean isStatic = n.endsWith(".jpg") || n.endsWith(".png") || n.endsWith(".jpeg");

                    if (!isMp4 && !isGif && !isStatic) continue;

                    boolean isPreview = false;
                    if (isGif) {
                        String baseName = f.getName().substring(0, f.getName().lastIndexOf("."));
                        File matchingMp4 = new File(assetsDir, baseName + ".mp4");
                        if (matchingMp4.exists()) isPreview = true;
                    }
                    
                    if (isPreview) continue;

                    boolean show = filter.equals("ALL") ||
                                   (filter.equals("VID") && isMp4) ||
                                   (filter.equals("GIF") && isGif) ||
                                   (filter.equals("IMG") && isStatic);

                    if (show) addCard(f);
                }
            }
        }
        
        gridPanel.revalidate();
        gridPanel.repaint();
        checkPreviews();
    }

    private String removeExtension(String filename) {
        int lastDot = filename.lastIndexOf(".");
        if (lastDot > 0) return filename.substring(0, lastDot);
        return filename;
    }

    private void addCard(File f) {
        JPanel card = new JPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(260, 240));
        card.setBackground(CARD_BG);
        card.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(8, 8, 8, 8)
        ));

        JLabel thumb = new JLabel();
        thumb.setHorizontalAlignment(SwingConstants.CENTER);
        thumb.setBackground(BG_MAIN);
        thumb.setOpaque(true);
        thumb.setPreferredSize(new Dimension(244, 170));

        ImageIcon icon = getThumbnail(f);
        if (icon != null) thumb.setIcon(icon);
        else {
            thumb.setText("Loading...");
            thumb.setForeground(TEXT_SECONDARY);
        }

        JPanel info = new JPanel(new BorderLayout());
        info.setBackground(CARD_BG);
        info.setBorder(new EmptyBorder(10, 5, 5, 5));

        JLabel name = new JLabel(removeExtension(f.getName()));
        name.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        name.setForeground(TEXT_PRIMARY);

        info.add(name, BorderLayout.CENTER);
        card.add(thumb, BorderLayout.CENTER);
        card.add(info, BorderLayout.SOUTH);

        thumb.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { applyWallpaper(f); }
            public void mouseEntered(MouseEvent e) { 
                card.setBorder(new CompoundBorder(
                    new LineBorder(ACCENT, 2, true),
                    new EmptyBorder(7, 7, 7, 7)
                ));
            }
            public void mouseExited(MouseEvent e) { 
                card.setBorder(new CompoundBorder(
                    new LineBorder(BORDER_COLOR, 1, true),
                    new EmptyBorder(8, 8, 8, 8)
                ));
            }
        });

        gridPanel.add(card);
    }

    private ImageIcon getThumbnail(File f) {
        String n = f.getName().toLowerCase();
        try {
            if (n.endsWith(".mp4") || n.endsWith(".avi")) {
                String baseName = f.getName().substring(0, f.getName().lastIndexOf("."));
                File gif = new File(assetsDir, baseName + ".gif");
                if (gif.exists()) return new ImageIcon(gif.getAbsolutePath());
                return null;
            } else if (n.endsWith(".gif")) {
                return new ImageIcon(f.getAbsolutePath());
            } else {
                ImageIcon icon = new ImageIcon(f.getAbsolutePath());
                Image scaled = icon.getImage().getScaledInstance(244, 170, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            }
        } catch (Exception e) { 
            return null; 
        }
    }

    private void checkPreviews() {
        new Thread(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder("python", "../fix_previews.py", assetsDir.getAbsolutePath());
                pb.start().waitFor();
            } catch (Exception e) {}
        }).start();
    }

    // Kill all Python wallpaper processes
    private void killAllWallpapers() {
        try {
            // Method 1: Kill by command line
            ProcessBuilder pb1 = new ProcessBuilder("cmd.exe", "/c", 
                "for /f \"tokens=2\" %i in ('tasklist /FI \"IMAGENAME eq python.exe\" /FO LIST ^| findstr \"PID:\"') do taskkill /F /PID %i");
            pb1.start().waitFor();
            
            // Method 2: Kill all python.exe processes
            ProcessBuilder pb2 = new ProcessBuilder("taskkill", "/F", "/IM", "python.exe");
            pb2.start().waitFor();
            
            Thread.sleep(800);
            
            System.out.println("Killed all wallpaper processes");
        } catch (Exception e) {
            System.out.println("Error killing wallpapers: " + e.getMessage());
        }
    }

    private void applyWallpaper(File f) {
        // Kill all previous wallpapers first
        killAllWallpapers();
        
        String n = f.getName().toLowerCase();
        String script = n.endsWith(".mp4") ? "video_wallpaper.py" : n.endsWith(".gif") ? "animated_wallpaper.py" : "set_static_wallpaper.py";
        statusLabel.setText("Applying: " + removeExtension(f.getName()));
        
        try {
            new ProcessBuilder("python", "../python_scripts/" + script, f.getAbsolutePath()).start();
        } catch (IOException e) { 
            e.printStackTrace(); 
        }
    }

    private void openNativeFileDialog() {
        FileDialog fd = new FileDialog(this, "Select Assets Folder", FileDialog.LOAD);
        fd.setDirectory(assetsDir.getAbsolutePath());
        System.setProperty("apple.awt.fileDialogForDirectories", "true");
        fd.setVisible(true);
        String selectedDir = fd.getDirectory();
        if (selectedDir != null) {
            assetsDir = new File(selectedDir);
            prefs.put("assetsFolder", assetsDir.getAbsolutePath());
            loadLibrary(currentFilter);
        }
        System.setProperty("apple.awt.fileDialogForDirectories", "false");
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
