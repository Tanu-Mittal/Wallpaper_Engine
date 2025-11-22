import java.awt.*;
import java.io.*;
import javax.swing.*;

public class MainWindow extends JFrame {
    private JTextField fileField;
    private JLabel previewLabel;

    public MainWindow() {
        // Use cross-platform look and feel for maximum color control
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ex) {}

        Color beige = new Color(245,237,215);
        Color brown = new Color(102,85,55);

        UIManager.put("OptionPane.background", beige);
        UIManager.put("Panel.background", beige);
        UIManager.put("OptionPane.messageForeground", brown);
        UIManager.put("Button.background", new Color(205,180,100));
        UIManager.put("Button.foreground", brown);
        UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.BOLD, 16));
        UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 14));
        UIManager.put("FileChooser.background", beige);
        UIManager.put("FileChooser.listViewBackground", new Color(252,244,228));
        UIManager.put("FileChooser.foreground", brown);
        UIManager.put("FileChooser.font", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("FileChooser.messageFont", new Font("Segoe UI", Font.BOLD, 15));

        setTitle("Wallboard • Wallpaper Cheatsheet");
        setSize(740, 440);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(238, 223, 185));

        // Title Panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(217, 204, 170));
        JLabel heading = new JLabel("📋 WALLBOARD – YOUR WALLPAPER CHEATSHEET", SwingConstants.CENTER);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 27));
        heading.setForeground(brown);
        titlePanel.add(heading);
        titlePanel.setBorder(BorderFactory.createMatteBorder(0,0,2,0, new Color(197,167,110)));
        add(titlePanel, BorderLayout.NORTH);

        // Card Panel
        JPanel cardPanel = new JPanel(new GridBagLayout());
        cardPanel.setBackground(new Color(238,223,185));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8,8,8,8);
        gbc.fill = GridBagConstraints.BOTH;

        // Picker Box
        JPanel pickerBox = new JPanel(new GridBagLayout());
        pickerBox.setBackground(beige);
        pickerBox.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(205,180,100),2),
            "Step 1: Select Your Wallpaper File", 0, 0,
            new Font("Segoe UI", Font.BOLD, 17), new Color(137, 101, 36)));
        GridBagConstraints pGbc = new GridBagConstraints();
        pGbc.insets = new Insets(3,3,3,3);
        JLabel pickLab = new JLabel("File path:");
        pickLab.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        pickLab.setForeground(brown);
        fileField = new JTextField(36);
        fileField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        fileField.setBackground(new Color(252,244,228));
        fileField.setBorder(BorderFactory.createLineBorder(new Color(205,180,100),2,true));
        JButton browseBtn = new JButton("Browse");
        browseBtn.setBackground(new Color(205,180,100));
        browseBtn.setForeground(brown);
        browseBtn.setFont(new Font("Segoe UI", Font.BOLD,14));
        pGbc.gridx=0; pGbc.gridy=0; pickerBox.add(pickLab, pGbc);
        pGbc.gridx=1; pGbc.gridy=0; pickerBox.add(fileField, pGbc);
        pGbc.gridx=2; pGbc.gridy=0; pickerBox.add(browseBtn, pGbc);

        JLabel hintPick = new JLabel("Tip: Supported formats – image (jpg/png), video (mp4), GIF animation.");
        hintPick.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hintPick.setForeground(new Color(163,133,81));
        pGbc.gridwidth=3; pGbc.gridy=1; pGbc.gridx=0; pickerBox.add(hintPick, pGbc);

        gbc.gridx=0; gbc.gridy=0; cardPanel.add(pickerBox, gbc);

        // Preview Box
        JPanel previewBox = new JPanel(new GridBagLayout());
        previewBox.setBackground(beige);
        previewBox.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(205,180,100),1),
            "Step 2: Preview (Image/GIF only)",
            0, 0, new Font("Segoe UI", Font.BOLD,14), new Color(137,101,36)));
        previewLabel = new JLabel("", SwingConstants.CENTER);
        previewLabel.setPreferredSize(new Dimension(180,110));
        previewLabel.setBorder(BorderFactory.createLineBorder(new Color(224,208,163),2));
        previewLabel.setBackground(new Color(252,244,228));
        previewLabel.setOpaque(true);
        previewBox.add(previewLabel);
        gbc.gridx=0; gbc.gridy=1; cardPanel.add(previewBox, gbc);

        // Action Box
        JPanel actionBox = new JPanel(new GridBagLayout());
        actionBox.setBackground(beige);
        actionBox.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(205,180,100),2),
            "Step 3: Choose Wallpaper Type & Apply",
            0, 0, new Font("Segoe UI", Font.BOLD,16), new Color(137,101,36)));
        GridBagConstraints aGbc = new GridBagConstraints();
        aGbc.insets = new Insets(8,14,8,14);

        JButton staticBtn = new JButton("📷 Image Wallpaper");
        JButton videoBtn = new JButton("🎥 Video Wallpaper");
        JButton animatedBtn = new JButton("🌀 GIF Animation");
        for(JButton btn:new JButton[]{staticBtn,videoBtn,animatedBtn}) {
            btn.setBackground(new Color(174,150,103));
            btn.setForeground(Color.WHITE);
            btn.setFont(new Font("Segoe UI", Font.BOLD,15));
            btn.setBorder(BorderFactory.createEmptyBorder(10,30,10,30));
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        aGbc.gridx=0; aGbc.gridy=0; actionBox.add(staticBtn,aGbc);
        aGbc.gridx=1; aGbc.gridy=0; actionBox.add(videoBtn,aGbc);
        aGbc.gridx=2; aGbc.gridy=0; actionBox.add(animatedBtn,aGbc);

        JLabel helpMsg = new JLabel("<html>Image: Sets desktop background.<br>Video: Overlays animated video.<br>GIF: Animated overlay.<br><br>Pro tip: Try each for desktop magic!</html>");
        helpMsg.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        helpMsg.setForeground(new Color(163,133,81));
        aGbc.gridx=0; aGbc.gridy=1; aGbc.gridwidth=3; actionBox.add(helpMsg,aGbc);

        gbc.gridx=0; gbc.gridy=2; cardPanel.add(actionBox, gbc);

        add(cardPanel, BorderLayout.CENTER);

        // Browse handler
        browseBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser("../assets");
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setDialogTitle("Pick any wallpaper file!");
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Images/Videos/GIF", "jpg", "jpeg", "bmp", "png", "mp4", "avi", "gif"));
            int r = chooser.showOpenDialog(this);
            if (r == JFileChooser.APPROVE_OPTION) {
                File pick = chooser.getSelectedFile();
                fileField.setText(pick.getAbsolutePath());
                String ext = pick.getName().toLowerCase();
                if (ext.endsWith(".jpg") || ext.endsWith(".jpeg") ||
                    ext.endsWith(".png") || ext.endsWith(".bmp") ||
                    ext.endsWith(".gif")) {
                    ImageIcon icon = new ImageIcon(pick.getAbsolutePath());
                    Image img = icon.getImage().getScaledInstance(180,110,Image.SCALE_SMOOTH);
                    previewLabel.setIcon(new ImageIcon(img));
                } else {
                    previewLabel.setIcon(null);
                }
            }
        });

        // Start/stop overlays on every action
        staticBtn.addActionListener(e -> {
            stopActiveWallpaper();
            showBeigeMessage("Wallboard Info",
                setWallpaper("../python_scripts/set_static_wallpaper.py"));
        });
        videoBtn.addActionListener(e -> {
            stopActiveWallpaper();
            showBeigeMessage("Wallboard Info",
                setWallpaper("../python_scripts/video_wallpaper.py"));
        });
        animatedBtn.addActionListener(e -> {
            stopActiveWallpaper();
            showBeigeMessage("Wallboard Info",
                setWallpaper("../python_scripts/animated_wallpaper.py"));
        });
    }

    // Stop both video and animation overlays before switching wallpaper
    private void stopActiveWallpaper() {
        try {
            String[] cmd1 = {"python", "../python_scripts/stop_video.py"};
            new ProcessBuilder(cmd1).redirectErrorStream(true).start().waitFor();
        } catch (Exception ex) {}
        try {
            String[] cmd2 = {"python", "../python_scripts/stop_animation.py"};
            new ProcessBuilder(cmd2).redirectErrorStream(true).start().waitFor();
        } catch (Exception ex) {}
    }

    // Call Python script and return output for popup
    private String setWallpaper(String pyScript) {
        String filePath = fileField.getText().trim();
        previewLabel.setIcon(null);
        if (filePath.isEmpty()) {
            return "Please pick a wallpaper file first.";
        }
        File scriptFile = new File(pyScript);
        if (!scriptFile.exists()) {
            return "Wallpaper script missing: " + pyScript;
        }
        try {
            String[] cmd = {"python", pyScript, filePath};
            Process process = new ProcessBuilder(cmd)
                .redirectErrorStream(true)
                .start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) output.append(line).append("\n");
            process.waitFor();
            return output.toString();
        } catch (Exception ex) {
            return "Error: " + ex.getMessage();
        }
    }

    // Custom themed message dialog
    public void showBeigeMessage(String title, String message) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.getContentPane().setBackground(new Color(245,237,215));
        dialog.setLayout(new BorderLayout());
        JLabel msgLabel = new JLabel("<html><div style='text-align:center;'>" + message.replace("\n","<br>") + "</div></html>", SwingConstants.CENTER);
        msgLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        msgLabel.setForeground(new Color(102,85,55));
        dialog.add(msgLabel, BorderLayout.CENTER);

        JButton okBtn = new JButton("OK");
        okBtn.setBackground(new Color(205,180,100));
        okBtn.setForeground(new Color(102,85,55));
        okBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        okBtn.addActionListener(e -> dialog.dispose());
        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(new Color(245,237,215));
        btnPanel.add(okBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setSize(400, 160);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainWindow().setVisible(true));
    }
}
