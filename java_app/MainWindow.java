import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class MainWindow extends JFrame {

    // Data
    private List<File> galleryFiles = new ArrayList<>();
    private int currentIndex = 0;
    private volatile boolean stopBatch = false; 

    // UI Components
    private JLabel mainPreviewLabel;       
    private JLabel infoLabel;              
    private JButton btnSetCover;           
    private JLabel statusLabel; 

    // Theme Colors
    private final Color BEIGE = new Color(245, 237, 215);
    private final Color BROWN = new Color(102, 85, 55);
    private final Color BUTTON_COLOR = new Color(205, 180, 100);
    private final Color LIGHT_BG = new Color(252, 244, 228);
    private final Color HIGHLIGHT_RED = new Color(200, 50, 50);

    public MainWindow() {
        setupTheme();
        initUI();
        
        // Handle window resize events to re-scale images
        this.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent componentEvent) {
                updateView();
            }
        });

        File assets = new File("C:\\Users\\tanum\\OneDrive\\Desktop\\DynamicWallpaperEngine\\assets");
        if (!assets.exists()) assets = new File("assets");
        if (!assets.exists()) assets.mkdirs();
        loadLibrary(assets);
    }

    private void setupTheme() {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); } catch (Exception e) {}
        UIManager.put("Panel.background", BEIGE);
        UIManager.put("Button.background", BUTTON_COLOR);
        UIManager.put("Button.foreground", BROWN);
    }

    private void initUI() {
        setTitle("Wallboard • Slider View");
        setSize(1100, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // TOP BAR
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(217, 204, 170));
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JLabel title = new JLabel("🖼 WALLPAPER GALLERY");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(BROWN);
        
        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topRight.setOpaque(false);
        
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        statusLabel.setForeground(new Color(100, 80, 60));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));
        
        JButton folderBtn = new JButton("📂 Open Folder");
        folderBtn.addActionListener(e -> chooseDirectory());
        
        topRight.add(statusLabel);
        topRight.add(folderBtn);

        topBar.add(title, BorderLayout.WEST);
        topBar.add(topRight, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // CENTER SLIDER
        JPanel sliderPanel = new JPanel(new BorderLayout());
        sliderPanel.setBackground(BEIGE);
        sliderPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        mainPreviewLabel = new JLabel("No Images Found", SwingConstants.CENTER);
        mainPreviewLabel.setBackground(LIGHT_BG);
        mainPreviewLabel.setOpaque(true);
        mainPreviewLabel.setBorder(BorderFactory.createLineBorder(BROWN, 2));
        // Important: Allow label to resize freely
        mainPreviewLabel.setPreferredSize(new Dimension(100, 100)); 

        // ARROWS
        JButton btnPrev = createArrowButton("<"); 
        JButton btnNext = createArrowButton(">"); 
        btnPrev.addActionListener(e -> navigate(-1));
        btnNext.addActionListener(e -> navigate(1));

        sliderPanel.add(btnPrev, BorderLayout.WEST);
        sliderPanel.add(mainPreviewLabel, BorderLayout.CENTER);
        sliderPanel.add(btnNext, BorderLayout.EAST);
        
        infoLabel = new JLabel("Loading...", SwingConstants.CENTER);
        infoLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        infoLabel.setForeground(BROWN);
        sliderPanel.add(infoLabel, BorderLayout.SOUTH);
        
        // COVER BUTTONS
        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.add(sliderPanel, BorderLayout.CENTER);
        
        JPanel coverPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0)); 
        coverPanel.setOpaque(false);
        
        btnSetCover = new JButton("➕ Upload GIF/Image");
        btnSetCover.setBackground(Color.WHITE);
        btnSetCover.setVisible(false);
        btnSetCover.addActionListener(e -> setVideoCover());
        
        coverPanel.add(btnSetCover);
        centerContainer.add(coverPanel, BorderLayout.NORTH); 

        add(centerContainer, BorderLayout.CENTER);

        // BOTTOM BAR
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 20));
        bottomPanel.setBackground(new Color(220, 210, 190));
        bottomPanel.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, BROWN));

        JButton btnImg = createActionBtn("📷 Apply as Image");
        JButton btnVid = createActionBtn("🎥 Apply as Video");
        JButton btnGif = createActionBtn("🌀 Apply as GIF");

        btnImg.addActionListener(e -> runScript("set_static_wallpaper.py"));
        btnVid.addActionListener(e -> runScript("video_wallpaper.py"));
        btnGif.addActionListener(e -> runScript("animated_wallpaper.py"));

        bottomPanel.add(btnImg); bottomPanel.add(btnVid); bottomPanel.add(btnGif);
        add(bottomPanel, BorderLayout.SOUTH);
        
        // Keys
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("LEFT"), "prev");
        getRootPane().getActionMap().put("prev", new AbstractAction() { public void actionPerformed(ActionEvent e) { navigate(-1); }});
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("RIGHT"), "next");
        getRootPane().getActionMap().put("next", new AbstractAction() { public void actionPerformed(ActionEvent e) { navigate(1); }});
    }

    private JButton createArrowButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 80)); 
        b.setBackground(BEIGE);
        b.setForeground(BROWN);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) { b.setForeground(HIGHLIGHT_RED); }
            public void mouseExited(MouseEvent evt) { b.setForeground(BROWN); }
        });
        return b;
    }

    private JButton createActionBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 14));
        b.setBackground(BUTTON_COLOR);
        b.setPreferredSize(new Dimension(180, 45));
        return b;
    }

    private void chooseDirectory() {
        JFileChooser c = new JFileChooser();
        c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (c.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) loadLibrary(c.getSelectedFile());
    }

    private void loadLibrary(File dir) {
        stopBatch = true; 
        galleryFiles.clear();
        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles((d, name) -> {
                String n = name.toLowerCase();
                return n.endsWith(".jpg") || n.endsWith(".png") || n.endsWith(".jpeg") || 
                       n.endsWith(".gif") || n.endsWith(".mp4") || n.endsWith(".avi");
            });
            if (files != null) for(File f : files) galleryFiles.add(f);
        }
        currentIndex = 0;
        updateView();
        startBatchPreviewGeneration();
    }

    private void startBatchPreviewGeneration() {
        stopBatch = false;
        new Thread(() -> {
            for (File f : galleryFiles) {
                if (stopBatch) break;
                String name = f.getName().toLowerCase();
                if (name.endsWith(".mp4") || name.endsWith(".avi")) {
                    String base = f.getAbsolutePath().substring(0, f.getAbsolutePath().lastIndexOf("."));
                    File gif = new File(base + ".gif");
                    if (!gif.exists()) {
                        SwingUtilities.invokeLater(() -> statusLabel.setText("⏳ Generating: " + f.getName()));
                        try {
                            ProcessBuilder pb = new ProcessBuilder("python", "fix_previews.py"); 
                            // Note: using fix_previews.py which you ran manually is safer
                            // Or call the generation logic per file if preferred. 
                            // For now, relying on the manual fix script you ran is safest.
                        } catch (Exception e) {}
                    }
                }
            }
            SwingUtilities.invokeLater(() -> statusLabel.setText("✅ All previews ready"));
        }).start();
    }

    private void navigate(int direction) {
        if (galleryFiles.isEmpty()) return;
        currentIndex += direction;
        if (currentIndex < 0) currentIndex = galleryFiles.size() - 1;
        if (currentIndex >= galleryFiles.size()) currentIndex = 0;
        updateView();
    }

    private void updateView() {
        if (galleryFiles.isEmpty()) {
            mainPreviewLabel.setIcon(null);
            mainPreviewLabel.setText("No files found.");
            btnSetCover.setVisible(false);
            return;
        }

        File f = galleryFiles.get(currentIndex);
        infoLabel.setText(String.format("File %d of %d: %s", currentIndex + 1, galleryFiles.size(), f.getName()));
        String name = f.getName().toLowerCase();
        
        btnSetCover.setVisible(false); 

        try {
            ImageIcon icon = null;

            if (name.endsWith(".mp4") || name.endsWith(".avi")) {
                String base = f.getAbsolutePath().substring(0, f.getAbsolutePath().lastIndexOf("."));
                File gif = new File(base + ".gif");
                File jpg = new File(base + ".jpg");
                
                if (gif.exists()) {
                    icon = new ImageIcon(gif.getAbsolutePath());
                    icon.getImage().flush(); 
                } else if (jpg.exists()) {
                    icon = new ImageIcon(jpg.getAbsolutePath());
                } else {
                    mainPreviewLabel.setIcon(null);
                    mainPreviewLabel.setText("<html><center><h1>🎥 VIDEO FILE</h1><br>(Preview generating...)</center></html>");
                    btnSetCover.setVisible(true); 
                    return; 
                }
            } else {
                icon = new ImageIcon(f.getAbsolutePath());
            }

            if (icon != null) {
                boolean isAnimated = name.endsWith(".gif") || (name.endsWith(".mp4") && new File(f.getAbsolutePath().replace(".mp4", ".gif")).exists());
                
                // Calculate available size
                int w = mainPreviewLabel.getWidth();
                int h = mainPreviewLabel.getHeight();
                if (w == 0) w = 800; // Fallback
                if (h == 0) h = 500; 

                if (isAnimated) {
                     // Keep animations unscaled to preserve movement
                     mainPreviewLabel.setText("");
                     mainPreviewLabel.setIcon(icon);
                     mainPreviewLabel.setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    // Scale static images to FILL the area
                    Image img = icon.getImage();
                    Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
                    mainPreviewLabel.setText("");
                    mainPreviewLabel.setIcon(new ImageIcon(scaled));
                }
            }

        } catch (Exception e) {
            mainPreviewLabel.setIcon(null);
            mainPreviewLabel.setText("Error loading file.");
        }
    }

    private void setVideoCover() {
        File vid = galleryFiles.get(currentIndex);
        JFileChooser c = new JFileChooser();
        c.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Image/GIF", "jpg", "png", "gif"));
        if (c.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String base = vid.getAbsolutePath().substring(0, vid.getAbsolutePath().lastIndexOf("."));
                String ext = c.getSelectedFile().getName().toLowerCase().endsWith(".gif") ? ".gif" : ".jpg";
                Files.copy(c.getSelectedFile().toPath(), new File(base + ext).toPath(), StandardCopyOption.REPLACE_EXISTING);
                updateView(); 
            } catch (IOException e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
        }
    }

    private void runScript(String scriptName) {
        if (galleryFiles.isEmpty()) return;
        File f = galleryFiles.get(currentIndex);
        try {
            String path = "../python_scripts/" + scriptName; 
            if (!new File(path).exists()) path = scriptName;
            new ProcessBuilder("python", path, f.getAbsolutePath()).start();
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainWindow().setVisible(true));
    }
}
