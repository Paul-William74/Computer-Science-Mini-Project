import graph.Graph;
import graph.Node;
import imageprocessing.ImageProcessor;
import similarity.KNNClassifier;
import similarity.TrainingLoader;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class MainUI extends JFrame {

    private JLabel originalPreview;
    private JLabel skeletonPreview;

    private JLabel resultLabel;
    private JLabel selectedFileLabel;
    private JLabel statusLabel;

    private JLabel comparisonValue;
    private JLabel outcomeValue;
    private JLabel recommendationValue;
    private JLabel bifurcationValue;

    private JTextArea explanationArea;

    private JButton loadButton;
    private JButton chooseButton;
    private JButton screenButton;

    private JProgressBar loadingBar;

    private File selectedFile;
    private KNNClassifier<Node, Integer> classifier;

    private Timer loadingTimer;
    private int loadingStep = 0;

    private final String[] loadingMessages = {
            "Preparing reference images...",
            "Reading retinal vessel patterns...",
            "Building skeleton image...",
            "Counting bifurcations...",
            "Generating screening result..."
    };

    public MainUI() {
        setTitle("MediGraph - Retinal Vessel Screening");
        setSize(1450, 850);
        setMinimumSize(new Dimension(1100, 700));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(18, 18));
        root.setBorder(new EmptyBorder(18, 18, 18, 18));
        root.setBackground(new Color(236, 244, 255));

        root.add(createHeader(), BorderLayout.NORTH);
        root.add(createMainPanel(), BorderLayout.CENTER);

        setContentPane(root);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(12, 74, 110));
        header.setBorder(new EmptyBorder(24, 30, 24, 30));

        JLabel title = new JLabel("MediGraph");
        title.setFont(new Font("SansSerif", Font.BOLD, 42));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Retinal Vessel Screening System");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 19));
        subtitle.setForeground(new Color(224, 242, 254));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(title);
        text.add(Box.createVerticalStrut(6));
        text.add(subtitle);

        header.add(text, BorderLayout.WEST);

        return header;
    }

    private JPanel createMainPanel() {
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);

        JScrollPane leftScroll = new JScrollPane(createLeftPanel());
        leftScroll.setBorder(null);
        leftScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        leftScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        leftScroll.getVerticalScrollBar().setUnitIncrement(16);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                leftScroll,
                createRightPanel()
        );

        splitPane.setResizeWeight(0.58);
        splitPane.setDividerLocation(800);
        splitPane.setDividerSize(7);
        splitPane.setContinuousLayout(true);
        splitPane.setBorder(null);

        container.add(splitPane, BorderLayout.CENTER);

        return container;
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setBackground(Color.WHITE);
        panel.setBorder(cardBorder("Screening Dashboard"));

        panel.add(createControlsPanel(), BorderLayout.NORTH);
        panel.add(createResultsPanel(), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createControlsPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));

        loadButton = new JButton("Load Reference Images");
        chooseButton = new JButton("Choose Eye Image");
        screenButton = new JButton("Run Screening");

        styleButton(loadButton, new Color(37, 99, 235));
        styleButton(chooseButton, new Color(22, 163, 74));
        styleButton(screenButton, new Color(220, 38, 38));

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 8));
        row.setBackground(Color.WHITE);
        row.add(loadButton);
        row.add(chooseButton);
        row.add(screenButton);

        selectedFileLabel = new JLabel("Selected image: None");
        selectedFileLabel.setFont(new Font("SansSerif", Font.PLAIN, 15));

        statusLabel = new JLabel("Status: Ready");
        statusLabel.setFont(new Font("SansSerif", Font.ITALIC, 15));
        statusLabel.setForeground(new Color(75, 85, 99));

        loadingBar = new JProgressBar();
        loadingBar.setIndeterminate(true);
        loadingBar.setVisible(false);
        loadingBar.setStringPainted(true);

        loadButton.addActionListener(e -> loadReferenceImages());
        chooseButton.addActionListener(e -> chooseImage());
        screenButton.addActionListener(e -> runScreening());

        panel.add(row);
        panel.add(Box.createVerticalStrut(8));
        panel.add(selectedFileLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(statusLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(loadingBar);

        return panel;
    }

    private JPanel createResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(12, 18, 18, 18));

        resultLabel = new JLabel("Result: Not screened yet");
        resultLabel.setOpaque(true);
        resultLabel.setBackground(new Color(226, 232, 240));
        resultLabel.setForeground(new Color(30, 41, 59));
        resultLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        resultLabel.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel metrics = new JPanel(new GridLayout(2, 2, 12, 12));
        metrics.setBackground(Color.WHITE);

        comparisonValue = new JLabel("-");
        outcomeValue = new JLabel("-");
        recommendationValue = new JLabel("-");
        bifurcationValue = new JLabel("-");

        metrics.add(metricCard("Image Comparison", comparisonValue, new Color(219, 234, 254)));
        metrics.add(metricCard("Screening Outcome", outcomeValue, new Color(255, 247, 237)));
        metrics.add(metricCard("Estimated Bifurcation Count", bifurcationValue, new Color(224, 242, 254)));
        metrics.add(metricCard("Recommendation", recommendationValue, new Color(255, 237, 213)));

        explanationArea = new JTextArea();
        explanationArea.setEditable(false);
        explanationArea.setLineWrap(true);
        explanationArea.setWrapStyleWord(true);
        explanationArea.setFont(new Font("SansSerif", Font.PLAIN, 16));
        explanationArea.setBackground(new Color(248, 250, 252));
        explanationArea.setText(
                "Load the reference images, choose a retinal image, then run screening.\n\n" +
                        "After screening, the original image and skeletonized vessel structure will be displayed."
        );

        JScrollPane explanationScroll = new JScrollPane(explanationArea);
        explanationScroll.setBorder(new TitledBorder("Explanation"));
        explanationScroll.setPreferredSize(new Dimension(500, 260));

        JPanel center = new JPanel(new BorderLayout(12, 12));
        center.setBackground(Color.WHITE);
        center.add(metrics, BorderLayout.NORTH);
        center.add(explanationScroll, BorderLayout.CENTER);

        panel.add(resultLabel, BorderLayout.NORTH);
        panel.add(center, BorderLayout.CENTER);

        return panel;
    }

    private JPanel metricCard(String title, JLabel valueLabel, Color bg) {
        JPanel card = new JPanel(new BorderLayout(4, 4));
        card.setBackground(bg);
        card.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        titleLabel.setForeground(new Color(71, 85, 105));

        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        valueLabel.setForeground(new Color(15, 23, 42));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 12, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(cardBorder("Image Preview"));

        originalPreview = previewLabel("Original image will appear here");
        skeletonPreview = previewLabel("Skeletonized vessel structure will appear after screening");

        panel.add(wrapPreview("Original Retinal Image", originalPreview));
        panel.add(wrapPreview("Skeletonized Vessel Structure", skeletonPreview));

        return panel;
    }

    private JPanel wrapPreview(String title, JLabel label) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new TitledBorder(title));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private JLabel previewLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBackground(new Color(248, 250, 252));
        label.setBorder(new LineBorder(new Color(203, 213, 225), 1));
        label.setFont(new Font("SansSerif", Font.PLAIN, 16));
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setVerticalAlignment(JLabel.CENTER);
        return label;
    }

    private Border cardBorder(String title) {
        return new CompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1, true),
                new CompoundBorder(
                        new TitledBorder(
                                new LineBorder(new Color(203, 213, 225), 1, true),
                                title,
                                TitledBorder.LEFT,
                                TitledBorder.TOP,
                                new Font("SansSerif", Font.BOLD, 17),
                                new Color(12, 74, 110)
                        ),
                        new EmptyBorder(8, 8, 8, 8)
                )
        );
    }

    private void styleButton(JButton button, Color color) {
        button.setFocusPainted(false);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(200, 46));
    }

    private void loadReferenceImages() {
        setLoading(true, "Status: Loading reference images...");
        setButtonsEnabled(false);

        SwingWorker<KNNClassifier<Node, Integer>, Void> worker = new SwingWorker<>() {
            @Override
            protected KNNClassifier<Node, Integer> doInBackground() {
                return TrainingLoader.loadTrainingData(3);
            }

            @Override
            protected void done() {
                try {
                    classifier = get();
                    setLoading(false, "Status: Reference images loaded successfully.");
                    setButtonsEnabled(true);

                } catch (Exception e) {
                    setLoading(false, "Status: Reference images failed to load.");
                    setButtonsEnabled(true);
                    JOptionPane.showMessageDialog(MainUI.this, "Error loading reference images: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };

        worker.execute();
    }

    private void chooseImage() {
        JFileChooser chooser = new JFileChooser();

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedFile = chooser.getSelectedFile();

            selectedFileLabel.setText("Selected image: " + selectedFile.getName());
            statusLabel.setText("Status: Image selected. Run screening when ready.");

            showImage(selectedFile, originalPreview);

            skeletonPreview.setText("Skeletonized vessel structure will appear after screening");
            skeletonPreview.setIcon(null);

            resetResults();
        }
    }

    private void resetResults() {
        resultLabel.setText("Result: Ready for screening");
        resultLabel.setBackground(new Color(226, 232, 240));
        resultLabel.setForeground(new Color(30, 41, 59));

        comparisonValue.setText("-");
        outcomeValue.setText("-");
        recommendationValue.setText("-");
        bifurcationValue.setText("-");

        explanationArea.setText("The selected image is ready. Click Run Screening to generate the result.");
    }

    private void runScreening() {
        if (classifier == null) {
            JOptionPane.showMessageDialog(this, "Please load reference images first.");
            return;
        }

        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this, "Please choose an eye image first.");
            return;
        }

        setLoading(true, "Status: Screening retinal image...");
        setButtonsEnabled(false);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private String prediction;
            private int bifurcations;

            @Override
            protected Void doInBackground() {
                ImageProcessor processor = new ImageProcessor();
                Graph<Node, Integer> graph = processor.buildGraph(selectedFile);

                prediction = classifier.predict(graph);
                bifurcations = processor.getLastBifurcationCount();

                return null;
            }

            @Override
            protected void done() {
                updateResults(prediction, bifurcations);
                showImage(new File("Datasets/output/skeleton.png"), skeletonPreview);

                setLoading(false, "Status: Screening completed.");
                setButtonsEnabled(true);
            }
        };

        worker.execute();
    }

    private void updateResults(String prediction, int bifurcations) {
        boolean healthy = prediction.equalsIgnoreCase("healthy");

        bifurcationValue.setText(String.valueOf(bifurcations));

        if (healthy) {
            resultLabel.setText("Result: HEALTHY PATTERN DETECTED");
            resultLabel.setBackground(new Color(220, 252, 231));
            resultLabel.setForeground(new Color(22, 101, 52));

            comparisonValue.setText("Similar to healthy references");
            outcomeValue.setText("Low concern");
            recommendationValue.setText("No urgent concern indicated");

            explanationArea.setText(
                    "The selected retinal image was compared with labelled reference images.\n\n" +
                            "The top preview shows the original retinal image. The bottom preview shows the skeletonized vessel structure created during image processing.\n\n" +
                            "The system detected an estimated " + bifurcations + " bifurcation points. Bifurcations are vessel branching points where the vessel structure splits.\n\n" +
                            "Based on the overall vessel pattern, this image most closely matched the healthy reference examples.\n\n" +
                            "This screening result is generated by the system and should not replace professional medical assessment."
            );

        } else {
            resultLabel.setText("Result: POSSIBLE DISEASED PATTERN DETECTED");
            resultLabel.setBackground(new Color(254, 226, 226));
            resultLabel.setForeground(new Color(153, 27, 27));

            comparisonValue.setText("Similar to diseased references");
            outcomeValue.setText("Higher concern");
            recommendationValue.setText("Further review advised");

            explanationArea.setText(
                    "The selected retinal image was compared with labelled reference images.\n\n" +
                            "The top preview shows the original retinal image. The bottom preview shows the skeletonized vessel structure created during image processing.\n\n" +
                            "The system detected " + bifurcations + " bifurcation points. Bifurcations are vessel branching points where the vessel structure splits.\n\n" +
                            "Based on the overall vessel pattern, this image most closely matched the diseased reference examples and should be reviewed further.\n\n" +
                            "This screening result is generated by the system and should not replace professional medical assessment."
            );
        }
    }

    private void showImage(File file, JLabel label) {
        try {
            BufferedImage img = ImageIO.read(file);

            if (img == null) {
                label.setText("Image preview not available");
                label.setIcon(null);
                return;
            }

            int labelWidth = label.getWidth();
            int labelHeight = label.getHeight();

            if (labelWidth <= 0) labelWidth = 500;
            if (labelHeight <= 0) labelHeight = 320;

            double scale = Math.min(
                    (double) labelWidth / img.getWidth(),
                    (double) labelHeight / img.getHeight()
            );

            int newWidth = (int) (img.getWidth() * scale);
            int newHeight = (int) (img.getHeight() * scale);

            Image scaled = img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);

            label.setText("");
            label.setIcon(new ImageIcon(scaled));

        } catch (Exception e) {
            label.setText("Image preview not available");
            label.setIcon(null);
        }
    }

    private void setLoading(boolean loading, String message) {
        loadingBar.setVisible(loading);
        statusLabel.setText(message);

        if (loading) {
            loadingStep = 0;
            loadingBar.setString(loadingMessages[0]);

            loadingTimer = new Timer(900, e -> {
                loadingStep = (loadingStep + 1) % loadingMessages.length;
                loadingBar.setString(loadingMessages[loadingStep]);
                statusLabel.setText("Status: " + loadingMessages[loadingStep]);
            });

            loadingTimer.start();

        } else {
            if (loadingTimer != null) {
                loadingTimer.stop();
            }

            loadingBar.setString("");
        }
    }

    private void setButtonsEnabled(boolean enabled) {
        loadButton.setEnabled(enabled);
        chooseButton.setEnabled(enabled);
        screenButton.setEnabled(enabled);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainUI().setVisible(true));
    }
}