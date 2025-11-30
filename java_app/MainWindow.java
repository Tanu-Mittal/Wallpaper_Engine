import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileSystemView;

public class MainWindow extends JFrame {
    // Data
    private List<File> galleryFiles = new ArrayList<>();
    private List<File> filteredFiles = new ArrayList<>();
    private String currentFilter = "ALL";
    private volatile boolean stopBatch = false;
    private boolean isDarkMode = false;
    
    // Track current wallpaper process
    private Process currentWallpaperProcess = null;
    private String currentWallpaperType = null;
    
    // Preferences
    private static final Preferences prefs = Preferences.userRoot().node("WallpaperEngine");
    private static final String PREF_LAST_DIR = "lastDirectory";
    private static final String PREF_THEME = "darkMode";
    
    // UI Components
    private JPanel mainContentPanel;
    private JPanel gridPanel;
    private JLabel statusLabel;
    private JPanel sideNav;
    private JLabel topCountLabel;
    
    // Light Theme Colors
    private final Color LIGHT_BG = new Color(245, 247, 250);
    private final Color LIGHT_NAV_BG = new Color(255, 255, 255);
    private final Color LIGHT_CARD_BG = new Color(255, 255, 255);
    private final Color LIGHT_TEXT = new Color(30, 41, 59);
    private final Color LIGHT_TEXT_SECONDARY = new Color(100, 116, 139);
    private final Color LIGHT_ACCENT = new Color(59, 130, 246);
    private final Color LIGHT_HOVER = new Color(241, 245, 249);
    private final Color LIGHT_BORDER = new Color(226, 232, 240);
    
    // Dark Theme Colors
    private final Color DARK_BG = new Color(15, 23, 42);
    private final Color DARK_NAV_BG = new Color(30, 41, 59);
    private final Color DARK_CARD_BG = new Color(30, 41, 59);
    private final Color DARK_TEXT = new Color(248, 250, 252);
    private final Color DARK_TEXT_SECONDARY = new Color(148, 163, 184);
    private final Color DARK_ACCENT = new Color(96, 165, 250);
    private final Color DARK_HOVER = new Color(51, 65, 85);
    private final Color DARK_BORDER = new Color(51, 65, 85);
    
    public MainWindow() {
        loadPreferences();
        setupTheme();
        initModernUI();
        loadSavedDirectory();
        
        // Add shutdown hook to clean up wallpaper processes
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            stopCurrentWallpaper();
        }));
    }
    
    private void loadPreferences() {
        isDarkMode = prefs.getBoolean(PREF_THEME, false);
    }
    
    private void savePreferences() {
        prefs.putBoolean(PREF_THEME, isDarkMode);
    }
    
    private void setupTheme() {
        try {
            // Set Windows Look and Feel for native file chooser
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}
    }
    
    private void initModernUI() {
        setTitle("Wallpaper Engine");
        setSize(1100, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(getCurrentBg());
        
        // Left Navigation Panel (20% width = 220px)
        sideNav = createNavigationPanel();
        add(sideNav, BorderLayout.WEST);
        
        // Main Content Area (80% width)
        mainContentPanel = new JPanel(new BorderLayout(0, 0));
        mainContentPanel.setBackground(getCurrentBg());
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Top Bar with status
        JPanel topBar = createTopBar();
        mainContentPanel.add(topBar, BorderLayout.NORTH);
        
        // Grid Panel for wallpaper previews
        JScrollPane scrollPane = createGridScrollPane();
        mainContentPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(mainContentPanel, BorderLayout.CENTER);
        
        // Slide-in animation on startup
        animateSlideIn();
    }
    
    private void animateSlideIn() {
        final Point originalLocation = getLocation();
        final int startY = -getHeight();
        setLocation(originalLocation.x, startY);
        
        Timer slideTimer = new Timer(10, null);
        slideTimer.addActionListener(new ActionListener() {
            int currentY = startY;
            public void actionPerformed(ActionEvent e) {
                currentY += (originalLocation.y - currentY) / 5 + 1;
                if (currentY >= originalLocation.y) {
                    currentY = originalLocation.y;
                    slideTimer.stop();
                }
                setLocation(originalLocation.x, currentY);
            }
        });
        slideTimer.start();
    }
    
    private JPanel createNavigationPanel() {
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBackground(getCurrentNavBg());
        nav.setPreferredSize(new Dimension(220, getHeight()));
        nav.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, getCurrentBorder()));
        
        // Logo/Title - Left aligned
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 20));
        header.setBackground(getCurrentNavBg());
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel logo = new JLabel("Wallpaper Engine");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        logo.setForeground(getCurrentText());
        header.add(logo);
        nav.add(header);
        
        // Separator
        nav.add(Box.createVerticalStrut(15));
        nav.add(createSeparator());
        nav.add(Box.createVerticalStrut(15));
        
        // Theme Toggle Button
        JButton themeBtn = createNavButton(isDarkMode ? "Light Mode" : "Dark Mode");
        themeBtn.addActionListener(e -> toggleTheme(themeBtn));
        nav.add(themeBtn);
        
        // Separator
        nav.add(Box.createVerticalStrut(15));
        nav.add(createSeparator());
        nav.add(Box.createVerticalStrut(15));
        
        nav.add(createSectionLabel("FILTERS"));
        nav.add(Box.createVerticalStrut(5));
        
        // Filter Buttons
        nav.add(createNavButton("All", "ALL"));
        nav.add(createNavButton("Images", "IMAGE"));
        nav.add(createNavButton("Videos", "VIDEO"));
        nav.add(createNavButton("GIF", "GIF"));
        
        // Separator
        nav.add(Box.createVerticalStrut(15));
        nav.add(createSeparator());
        nav.add(Box.createVerticalStrut(15));
        
        nav.add(createSectionLabel("ACTIONS"));
        nav.add(Box.createVerticalStrut(5));
        
        // Action Buttons
        JButton addBtn = createNavButton("Add Wallpaper");
        addBtn.addActionListener(e -> chooseDirectory());
        nav.add(addBtn);
        
        // Push remaining space to bottom
        nav.add(Box.createVerticalGlue());
        
        // Separator before status
        nav.add(createSeparator());
        
        // Status at bottom
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusLabel.setForeground(getCurrentTextSecondary());
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        nav.add(statusLabel);
        
        return nav;
    }
    
    private JSeparator createSeparator() {
        JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
        sep.setMaximumSize(new Dimension(220, 1));
        sep.setForeground(getCurrentBorder());
        sep.setBackground(getCurrentBorder());
        return sep;
    }
    
    private JButton createNavButton(String text) {
        return createNavButton(text, null);
    }
    
    private JButton createNavButton(String text, String filter) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setForeground(getCurrentText());
        btn.setBackground(getCurrentNavBg());
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(220, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        
        if (filter != null) {
            btn.addActionListener(e -> {
                applyFilter(filter);
                animateButtonClick(btn);
            });
        }
        
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(getCurrentHover());
                btn.setContentAreaFilled(true);
            }
            public void mouseExited(MouseEvent e) {
                btn.setContentAreaFilled(false);
            }
        });
        
        return btn;
    }
    
    private void animateButtonClick(JButton btn) {
        Color original = btn.getForeground();
        btn.setForeground(getCurrentAccent());
        Timer timer = new Timer(200, e -> btn.setForeground(original));
        timer.setRepeats(false);
        timer.start();
    }
    
    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(getCurrentTextSecondary());
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        return label;
    }
    
    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(getCurrentBg());
        bar.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel title = new JLabel("All Wallpapers");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(getCurrentText());
        
        topCountLabel = new JLabel(filteredFiles.size() + " items");
        topCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        topCountLabel.setForeground(getCurrentTextSecondary());
        topCountLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));
        
        bar.add(title, BorderLayout.WEST);
        bar.add(topCountLabel, BorderLayout.EAST);
        
        return bar;
    }
    
    private JScrollPane createGridScrollPane() {
        gridPanel = new JPanel(new GridLayout(0, 3, 15, 15));
        gridPanel.setBackground(getCurrentBg());
        
        JScrollPane scroll = new JScrollPane(gridPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(getCurrentBg());
        
        return scroll;
    }
    
    private void populateGrid() {
        gridPanel.removeAll();
        
        for (File file : filteredFiles) {
            JPanel card = createWallpaperCard(file);
            gridPanel.add(card);
        }
        
        gridPanel.revalidate();
        gridPanel.repaint();
    }
    
    private JPanel createWallpaperCard(File file) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(getCurrentCardBg());
        card.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(getCurrentBorder(), 1),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        card.setPreferredSize(new Dimension(250, 200));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Image preview container
        JPanel imageContainer = new JPanel(new BorderLayout());
        imageContainer.setBackground(isDarkMode ? new Color(15, 23, 42) : new Color(241, 245, 249));
        
        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        
        // Load thumbnail
        String fileType = getFileType(file);
        if (fileType.equals("VIDEO")) {
            loadVideoThumbnail(file, imageLabel, imageContainer);
        } else {
            loadThumbnail(file, imageLabel);
        }
        
        imageContainer.add(imageLabel, BorderLayout.CENTER);
        
        // Info panel
        JPanel infoPanel = new JPanel(new BorderLayout(8, 0));
        infoPanel.setBackground(getCurrentCardBg());
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        
        String fileName = file.getName();
        if (fileName.length() > 25) fileName = fileName.substring(0, 22) + "...";
        
        JLabel nameLabel = new JLabel(fileName);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nameLabel.setForeground(getCurrentText());
        
        JLabel typeLabel = new JLabel(fileType);
        typeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        typeLabel.setForeground(getCurrentTextSecondary());
        
        infoPanel.add(nameLabel, BorderLayout.CENTER);
        infoPanel.add(typeLabel, BorderLayout.EAST);
        
        card.add(imageContainer, BorderLayout.CENTER);
        card.add(infoPanel, BorderLayout.SOUTH);
        
        // Hover effect (no zoom, just border change)
        card.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                card.setBorder(new CompoundBorder(
                    BorderFactory.createLineBorder(getCurrentAccent(), 2),
                    BorderFactory.createEmptyBorder(0, 0, 0, 0)
                ));
            }
            
            public void mouseExited(MouseEvent e) {
                card.setBorder(new CompoundBorder(
                    BorderFactory.createLineBorder(getCurrentBorder(), 1),
                    BorderFactory.createEmptyBorder(0, 0, 0, 0)
                ));
            }
            
            public void mouseClicked(MouseEvent e) {
                applyWallpaper(file);
                animateCardClick(card);
            }
        });
        
        return card;
    }
    
    private void animateCardClick(JPanel card) {
        Timer flashTimer = new Timer(100, null);
        final int[] flashCount = {0};
        flashTimer.addActionListener(e -> {
            if (flashCount[0] % 2 == 0) {
                card.setBackground(getCurrentAccent());
            } else {
                card.setBackground(getCurrentCardBg());
            }
            flashCount[0]++;
            if (flashCount[0] >= 4) {
                flashTimer.stop();
                card.setBackground(getCurrentCardBg());
            }
        });
        flashTimer.start();
    }
    
    private void loadVideoThumbnail(File file, JLabel imageLabel, JPanel container) {
        // Check for existing thumbnails (generated by fix_previews.py)
        String base = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf("."));
        File gifPreview = new File(base + ".gif");
        File jpgPreview = new File(base + ".jpg");
        
        BufferedImage thumbnail = null;
        
        if (jpgPreview.exists()) {
            try {
                thumbnail = ImageIO.read(jpgPreview);
            } catch (IOException e) {}
        } else if (gifPreview.exists()) {
            try {
                ImageIcon icon = new ImageIcon(gifPreview.getAbsolutePath());
                Image img = icon.getImage();
                Image scaled = img.getScaledInstance(250, 140, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(scaled));
                
                // Add Windows 11 video tape overlay
                addVideoOverlay(container);
                return;
            } catch (Exception e) {}
        }
        
        if (thumbnail != null) {
            Image scaled = thumbnail.getScaledInstance(250, 140, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaled));
            
            // Add Windows 11 video tape overlay
            addVideoOverlay(container);
        } else {
            imageLabel.setText("VIDEO");
            imageLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            imageLabel.setForeground(getCurrentTextSecondary());
            
            // Generate thumbnail asynchronously
            generateVideoThumbnail(file, imageLabel, container);
        }
    }
    
    private void addVideoOverlay(JPanel container) {
        JPanel overlay = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Windows 11 style filmstrip frame
                int padding = 8;
                int frameWidth = 4;
                
                // Top and bottom bars
                g2.setColor(new Color(255, 255, 255, 200));
                g2.fillRect(padding, padding, getWidth() - 2*padding, frameWidth);
                g2.fillRect(padding, getHeight() - padding - frameWidth, getWidth() - 2*padding, frameWidth);
                
                // Perforations (holes)
                g2.setColor(new Color(0, 0, 0, 100));
                int holeSize = 3;
                int holeSpacing = 12;
                for (int x = padding + 5; x < getWidth() - padding; x += holeSpacing) {
                    g2.fillRect(x, padding + 1, holeSize, 2);
                    g2.fillRect(x, getHeight() - padding - 3, holeSize, 2);
                }
                
                // Play icon
                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;
                int playSize = 30;
                
                g2.setColor(new Color(255, 255, 255, 180));
                int[] xPoints = {centerX - playSize/3, centerX + playSize/2, centerX - playSize/3};
                int[] yPoints = {centerY - playSize/2, centerY, centerY + playSize/2};
                g2.fillPolygon(xPoints, yPoints, 3);
            }
        };
        overlay.setOpaque(false);
        overlay.setBounds(0, 0, container.getWidth(), container.getHeight());
        container.add(overlay, BorderLayout.CENTER);
        container.setComponentZOrder(overlay, 0);
    }
    
    private void generateVideoThumbnail(File file, JLabel imageLabel, JPanel container) {
        new Thread(() -> {
            try {
                String baseName = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf("."));
                File jpgPreview = new File(baseName + ".jpg");
                
                if (!jpgPreview.exists()) {
                    // Call Python script to generate thumbnail
                    ProcessBuilder pb = new ProcessBuilder("python", "generate_preview.py", file.getAbsolutePath());
                    Process p = pb.start();
                    p.waitFor();
                }
                
                if (jpgPreview.exists()) {
                    BufferedImage img = ImageIO.read(jpgPreview);
                    Image scaled = img.getScaledInstance(250, 140, Image.SCALE_SMOOTH);
                    SwingUtilities.invokeLater(() -> {
                        imageLabel.setIcon(new ImageIcon(scaled));
                        imageLabel.setText("");
                        addVideoOverlay(container);
                        container.revalidate();
                        container.repaint();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    private void loadThumbnail(File file, JLabel label) {
        String name = file.getName().toLowerCase();
        
        try {
            ImageIcon icon = null;
            
            if (name.endsWith(".gif")) {
                // For GIFs, extract first frame only for thumbnail
                ImageIcon gifIcon = new ImageIcon(file.getAbsolutePath());
                Image img = gifIcon.getImage();
                
                // Create BufferedImage from first frame
                BufferedImage bi = new BufferedImage(
                    img.getWidth(null), 
                    img.getHeight(null), 
                    BufferedImage.TYPE_INT_ARGB
                );
                Graphics2D g2 = bi.createGraphics();
                g2.drawImage(img, 0, 0, null);
                g2.dispose();
                
                Image scaled = bi.getScaledInstance(250, 140, Image.SCALE_SMOOTH);
                icon = new ImageIcon(scaled);
            } else {
                icon = new ImageIcon(file.getAbsolutePath());
                Image img = icon.getImage();
                Image scaled = img.getScaledInstance(250, 140, Image.SCALE_SMOOTH);
                icon = new ImageIcon(scaled);
            }
            
            if (icon != null && icon.getIconWidth() > 0) {
                label.setIcon(icon);
                label.setText("");
            }
        } catch (Exception e) {
            label.setText("ERROR");
            label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            label.setForeground(getCurrentTextSecondary());
        }
    }
    
    private String getFileType(File file) {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".jpeg")) return "IMAGE";
        if (name.endsWith(".gif")) return "GIF";
        if (name.endsWith(".mp4") || name.endsWith(".avi")) return "VIDEO";
        return "FILE";
    }
    
    private void applyFilter(String filter) {
        currentFilter = filter;
        filteredFiles.clear();
        
        for (File file : galleryFiles) {
            String type = getFileType(file);
            if (filter.equals("ALL") || type.equals(filter)) {
                filteredFiles.add(file);
            }
        }
        
        populateGrid();
        updateTopBar();
    }
    
    private void stopCurrentWallpaper() {
        // Stop previous wallpaper process
        if (currentWallpaperProcess != null && currentWallpaperProcess.isAlive()) {
            System.out.println("Stopping previous wallpaper process...");
            currentWallpaperProcess.destroy();
            try {
                currentWallpaperProcess.waitFor(2, java.util.concurrent.TimeUnit.SECONDS);
                if (currentWallpaperProcess.isAlive()) {
                    currentWallpaperProcess.destroyForcibly();
                }
            } catch (InterruptedException e) {
                currentWallpaperProcess.destroyForcibly();
            }
        }
        
        // Also try to stop via the stop scripts based on previous wallpaper type
        if (currentWallpaperType != null) {
            try {
                String stopScript = null;
                if (currentWallpaperType.equals("VIDEO")) {
                    stopScript = "stop_video.py";
                } else if (currentWallpaperType.equals("GIF")) {
                    stopScript = "stop_animation.py";
                }
                
                if (stopScript != null) {
                    String[] possiblePaths = {
                        "python_scripts/" + stopScript,
                        stopScript,
                        "../python_scripts/" + stopScript
                    };
                    
                    for (String path : possiblePaths) {
                        if (new File(path).exists()) {
                            ProcessBuilder pb = new ProcessBuilder("python", path);
                            pb.start().waitFor(1, java.util.concurrent.TimeUnit.SECONDS);
                            System.out.println("Executed stop script: " + stopScript);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error stopping previous wallpaper: " + e.getMessage());
            }
        }
    }
    
    private void applyWallpaper(File file) {
        String type = getFileType(file);
        String script = "";
        
        // Stop any currently running wallpaper
        stopCurrentWallpaper();
        
        switch (type) {
            case "IMAGE": 
                script = "set_static_wallpaper.py"; 
                currentWallpaperType = "IMAGE";
                break;
            case "VIDEO": 
                script = "video_wallpaper.py"; 
                currentWallpaperType = "VIDEO";
                break;
            case "GIF": 
                script = "animated_wallpaper.py"; 
                currentWallpaperType = "GIF";
                break;
            default:
                JOptionPane.showMessageDialog(this, 
                    "Unsupported file type: " + type,
                    "Error", JOptionPane.WARNING_MESSAGE);
                return;
        }
        
        System.out.println("Applying wallpaper: " + file.getAbsolutePath());
        System.out.println("File type: " + type);
        System.out.println("Script: " + script);
        
        runScript(script, file);
    }
    
    private void runScript(String scriptName, File file) {
        try {
            // Try multiple possible paths for the Python scripts
            String[] possiblePaths = {
                "python_scripts/" + scriptName,
                scriptName,
                "../python_scripts/" + scriptName,
                "python_scripts\\" + scriptName
            };
            
            String scriptPath = null;
            for (String path : possiblePaths) {
                if (new File(path).exists()) {
                    scriptPath = path;
                    break;
                }
            }
            
            if (scriptPath == null) {
                JOptionPane.showMessageDialog(this, 
                    "Script not found: " + scriptName + "\nPlease ensure Python scripts are in the python_scripts folder.",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Execute the Python script with the file path as argument
            ProcessBuilder pb = new ProcessBuilder("python", scriptPath, file.getAbsolutePath());
            pb.redirectErrorStream(true);
            currentWallpaperProcess = pb.start();
            
            // Read output for debugging
            new Thread(() -> {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(currentWallpaperProcess.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("[Python] " + line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
            
            statusLabel.setText("Applied: " + file.getName());
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error executing script: " + e.getMessage() + "\n\nMake sure Python is installed and in PATH.",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void toggleTheme(JButton themeBtn) {
        isDarkMode = !isDarkMode;
        savePreferences();
        themeBtn.setText(isDarkMode ? "Light Mode" : "Dark Mode");
        
        // Update all components
        getContentPane().setBackground(getCurrentBg());
        mainContentPanel.setBackground(getCurrentBg());
        gridPanel.setBackground(getCurrentBg());
        
        // Recreate navigation and grid
        remove(sideNav);
        sideNav = createNavigationPanel();
        add(sideNav, BorderLayout.WEST);
        populateGrid();
        
        revalidate();
        repaint();
    }
    
    private void chooseDirectory() {
        // Use native Windows file chooser
        JFileChooser chooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        
        // Start from last saved directory
        String lastDir = prefs.get(PREF_LAST_DIR, null);
        if (lastDir != null) {
            File lastFile = new File(lastDir);
            if (lastFile.exists()) {
                chooser.setCurrentDirectory(lastFile);
            }
        }
        
        chooser.setDialogTitle("Select Wallpaper Folder");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            prefs.put(PREF_LAST_DIR, selected.getAbsolutePath());
            loadLibrary(selected);
        }
    }
    
    private void loadSavedDirectory() {
        String lastDir = prefs.get(PREF_LAST_DIR, null);
        
        if (lastDir != null) {
            File dir = new File(lastDir);
            if (dir.exists()) {
                loadLibrary(dir);
                return;
            }
        }
        
        // Fallback to default
        File assets = new File("assets");
        if (!assets.exists()) {
            assets.mkdirs();
        }
        loadLibrary(assets);
    }
    
    private void loadLibrary(File dir) {
        galleryFiles.clear();
        
        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles((d, name) -> {
                String n = name.toLowerCase();
                return n.endsWith(".jpg") || n.endsWith(".png") || n.endsWith(".jpeg") ||
                       n.endsWith(".gif") || n.endsWith(".mp4") || n.endsWith(".avi");
            });
            
            if (files != null) {
                for (File f : files) galleryFiles.add(f);
            }
        }
        
        applyFilter(currentFilter);
        statusLabel.setText("Loaded " + galleryFiles.size() + " files from " + dir.getName());
    }
    
    private void updateTopBar() {
        topCountLabel.setText(filteredFiles.size() + " items");
    }
    
    // Theme getters
    private Color getCurrentBg() { return isDarkMode ? DARK_BG : LIGHT_BG; }
    private Color getCurrentNavBg() { return isDarkMode ? DARK_NAV_BG : LIGHT_NAV_BG; }
    private Color getCurrentCardBg() { return isDarkMode ? DARK_CARD_BG : LIGHT_CARD_BG; }
    private Color getCurrentText() { return isDarkMode ? DARK_TEXT : LIGHT_TEXT; }
    private Color getCurrentTextSecondary() { return isDarkMode ? DARK_TEXT_SECONDARY : LIGHT_TEXT_SECONDARY; }
    private Color getCurrentAccent() { return isDarkMode ? DARK_ACCENT : LIGHT_ACCENT; }
    private Color getCurrentHover() { return isDarkMode ? DARK_HOVER : LIGHT_HOVER; }
    private Color getCurrentBorder() { return isDarkMode ? DARK_BORDER : LIGHT_BORDER; }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainWindow().setVisible(true));
    }
}
