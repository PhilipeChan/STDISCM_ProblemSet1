import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class Simulator {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Particle Simulator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JLabel fpsLabel = new JLabel("FPS: 0.00"); // Initialize the FPS label
        Canvas canvas = new Canvas(fpsLabel); // Pass the FPS label to the Canvas
        frame.add(fpsLabel, BorderLayout.NORTH); // Add the FPS label to the frame
        frame.add(canvas, BorderLayout.CENTER);

        // Create the side panel with a layout that respects vertical space
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        setupInputPanel(inputPanel, canvas); // Add components to the panel

        // Make the side panel scrollable
        JScrollPane scrollPane = new JScrollPane(inputPanel);
        // Adjust the scrolling speed for vertical scrolling
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setPreferredSize(new Dimension(300, 720)); // Set a fixed height matching the canvas
        frame.add(scrollPane, BorderLayout.EAST);

        frame.pack();
        frame.setVisible(true);

        new Thread(() -> {
            final int targetFPS = 60;
            final long optimalTime = 1000000000 / targetFPS; // Nanoseconds per frame

            while (true) {
                long startTime = System.nanoTime();

                // Update the simulation and repaint the canvas
                canvas.updateParticles();
                canvas.repaint();

                long updateTime;
                long waitTime;
                do {
                    updateTime = System.nanoTime() - startTime;
                    waitTime = optimalTime - updateTime;
                } while (waitTime > 0);
            }
        }).start();
    }

    // Utility method to generate a random integer within a range
    private static int getRandomIntInRange(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }

    // Utility method to generate a random double within a range
    private static double getRandomDoubleInRange(double min, double max) {
        Random random = new Random();
        return min + (max - min) * random.nextDouble();
    }

    private static void setupInputPanel(JPanel panel, Canvas canvas) {
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add padding around the panel

        // Section for Adding Particles Between Points
        JPanel betweenPointsPanel = new JPanel();
        betweenPointsPanel.setLayout(new BoxLayout(betweenPointsPanel, BoxLayout.Y_AXIS));
        betweenPointsPanel.setBorder(BorderFactory.createTitledBorder("Add Particles (Between Points)"));
        JTextField nField = addLabeledTextField(betweenPointsPanel, "Number of Particles:", "");
        JTextField startXField = addLabeledTextField(betweenPointsPanel, "Start X:", "");
        JTextField startYField = addLabeledTextField(betweenPointsPanel, "Start Y:", "");
        JTextField endXField = addLabeledTextField(betweenPointsPanel, "End X:", "");
        JTextField endYField = addLabeledTextField(betweenPointsPanel, "End Y:", "");
        JTextField angleField = addLabeledTextField(betweenPointsPanel, "Angle:", "");
        JTextField velocityField = addLabeledTextField(betweenPointsPanel, "Velocity:", "");
        JButton addButton = new JButton("Add Particles");
        addButton.addActionListener(e -> {
            try {
                int n = Integer.parseInt(nField.getText());
                int startX = Integer.parseInt(startXField.getText());
                int startY = Integer.parseInt(startYField.getText());
                int endX = Integer.parseInt(endXField.getText());
                int endY = Integer.parseInt(endYField.getText());
                double angle = Double.parseDouble(angleField.getText());
                double velocity = Double.parseDouble(velocityField.getText());

                // Validate number of particles
                if (n < 1) throw new IllegalArgumentException("Number of particles must be at least 1.");

                // Validate x and y ranges
                if (startX < 0 || startX > 1280 || endX < 0 || endX > 1280 || startY < 0 || startY > 720 || endY < 0 || endY > 720) {
                    throw new IllegalArgumentException("X must be between 0 and 1280, Y must be between 0 and 720.");
                }

                canvas.addParticlesBetweenPoints(n, new Point(startX, startY), new Point(endX, endY), angle, velocity);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "Invalid input. Please enter valid numbers.");
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(panel, ex.getMessage());
            }
        });
        betweenPointsPanel.add(addButton);
        JButton addRandomParticlesButton = new JButton("Add Random Particles");
        addRandomParticlesButton.addActionListener(e -> {
            int n = getRandomIntInRange(1, 500); // Up to 500 particles
            Point start = new Point(getRandomIntInRange(0, 1280), getRandomIntInRange(0, 720));
            Point end = new Point(getRandomIntInRange(0, 1280), getRandomIntInRange(0, 720));
            double angle = getRandomDoubleInRange(0, 360);
            double velocity = getRandomDoubleInRange(50, 500); // Example range, adjust as needed

            canvas.addParticlesBetweenPoints(n, start, end, angle, velocity);
        });
        betweenPointsPanel.add(addRandomParticlesButton);
        panel.add(betweenPointsPanel);

        // Section for Adding Particles with Varying Angles
        JPanel varyingAnglesPanel = new JPanel();
        varyingAnglesPanel.setLayout(new BoxLayout(varyingAnglesPanel, BoxLayout.Y_AXIS));
        varyingAnglesPanel.setBorder(BorderFactory.createTitledBorder("Add Particles (Varying Angles)"));
        JTextField nAngleField = addLabeledTextField(varyingAnglesPanel, "Number of Particles:", "");
        JTextField startAngleField = addLabeledTextField(varyingAnglesPanel, "Start Angle:", "");
        JTextField endAngleField = addLabeledTextField(varyingAnglesPanel, "End Angle:", "");
        JTextField velocityAngleField = addLabeledTextField(varyingAnglesPanel, "Velocity:", "");
        JTextField xField = addLabeledTextField(varyingAnglesPanel, "X:", "");
        JTextField yField = addLabeledTextField(varyingAnglesPanel, "Y:", "");
        JButton addAngleButton = new JButton("Add Particles");
        addAngleButton.addActionListener(e -> {
            try {
                int n = Integer.parseInt(nAngleField.getText());
                double startAngle = Double.parseDouble(startAngleField.getText());
                double endAngle = Double.parseDouble(endAngleField.getText());
                double velocity = Double.parseDouble(velocityAngleField.getText());
                int x = Integer.parseInt(xField.getText());
                int y = Integer.parseInt(yField.getText());

                // Validate number of particles
                if (n < 1) throw new IllegalArgumentException("Number of particles must be at least 1.");

                // Validate x and y ranges
                if (x < 0 || x > 1280 || y < 0 || y > 720) {
                    throw new IllegalArgumentException("X must be between 0 and 1280, Y must be between 0 and 720.");
                }

                canvas.addParticlesVaryingAngles(n, new Point(x, y), startAngle, endAngle, velocity);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "Invalid input. Please enter valid numbers.");
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(panel, ex.getMessage());
            }
        });
        varyingAnglesPanel.add(addAngleButton);
        JButton addRandomParticlesVaryingAnglesButton = new JButton("Add Random Particles");
        addRandomParticlesVaryingAnglesButton.addActionListener(e -> {
            int n = getRandomIntInRange(1, 500); // Up to 500 particles
            Point start = new Point(getRandomIntInRange(0, 1280), getRandomIntInRange(0, 720));
            double startAngle = getRandomDoubleInRange(0, 360);
            double endAngle = getRandomDoubleInRange(0, 360);
            double velocity = getRandomDoubleInRange(50, 500); // Example range, adjust as needed

            canvas.addParticlesVaryingAngles(n, start, startAngle, endAngle, velocity);
        });
        varyingAnglesPanel.add(addRandomParticlesVaryingAnglesButton);
        panel.add(varyingAnglesPanel);

        // Section for Adding Particles with Varying Velocities
        JPanel varyingVelocitiesPanel = new JPanel();
        varyingVelocitiesPanel.setLayout(new BoxLayout(varyingVelocitiesPanel, BoxLayout.Y_AXIS));
        varyingVelocitiesPanel.setBorder(BorderFactory.createTitledBorder("Add Particles (Varying Velocities)"));
        JTextField nVelocityField = addLabeledTextField(varyingVelocitiesPanel, "Number of Particles:", "");
        JTextField startVelocityField = addLabeledTextField(varyingVelocitiesPanel, "Start Velocity:", "");
        JTextField endVelocityField = addLabeledTextField(varyingVelocitiesPanel, "End Velocity:", "");
        JTextField angleVelocityField = addLabeledTextField(varyingVelocitiesPanel, "Angle:", "");
        JTextField xVelocityField = addLabeledTextField(varyingVelocitiesPanel, "X:", "");
        JTextField yVelocityField = addLabeledTextField(varyingVelocitiesPanel, "Y:", "");
        JButton addVelocityButton = new JButton("Add Particles");
        addVelocityButton.addActionListener(e -> {
            try {
                int n = Integer.parseInt(nVelocityField.getText());
                double startVelocity = Double.parseDouble(startVelocityField.getText());
                double endVelocity = Double.parseDouble(endVelocityField.getText());
                double angle = Double.parseDouble(angleVelocityField.getText());
                int x = Integer.parseInt(xVelocityField.getText());
                int y = Integer.parseInt(yVelocityField.getText());

                // Validate number of particles
                if (n < 1) throw new IllegalArgumentException("Number of particles must be at least 1.");

                // Validate x and y ranges
                if (x < 0 || x > 1280 || y < 0 || y > 720) {
                    throw new IllegalArgumentException("X must be between 0 and 1280, Y must be between 0 and 720.");
                }

                canvas.addParticlesVaryingVelocities(n, new Point(x, y), angle, startVelocity, endVelocity);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "Invalid input. Please enter valid numbers.");
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(panel, ex.getMessage());
            }
        });
        varyingVelocitiesPanel.add(addVelocityButton);
        JButton addRandomParticlesVaryingVelocitiesButton = new JButton("Add Random Particles");
        addRandomParticlesVaryingVelocitiesButton.addActionListener(e -> {
            int n = getRandomIntInRange(1, 500); // Up to 500 particles
            Point start = new Point(getRandomIntInRange(0, 1280), getRandomIntInRange(0, 720));
            double angle = getRandomDoubleInRange(0, 360);
            double startVelocity = getRandomDoubleInRange(50, 275); // Example range, adjust as needed
            double endVelocity = getRandomDoubleInRange(275, 500); // Ensure endVelocity > startVelocity

            canvas.addParticlesVaryingVelocities(n, start, angle, startVelocity, endVelocity);
        });
        varyingVelocitiesPanel.add(addRandomParticlesVaryingVelocitiesButton);
        panel.add(varyingVelocitiesPanel);

        // Section for Adding Walls
        JPanel addWallPanel = new JPanel();
        addWallPanel.setLayout(new BoxLayout(addWallPanel, BoxLayout.Y_AXIS));
        addWallPanel.setBorder(BorderFactory.createTitledBorder("Add Wall"));
        JTextField x1Field = addLabeledTextField(addWallPanel, "X1:", "");
        JTextField y1Field = addLabeledTextField(addWallPanel, "Y1:", "");
        JTextField x2Field = addLabeledTextField(addWallPanel, "X2:", "");
        JTextField y2Field = addLabeledTextField(addWallPanel, "Y2:", "");
        JButton addWallButton = new JButton("Add Wall");
        addWallButton.addActionListener(e -> {
            try {
                int x1 = Integer.parseInt(x1Field.getText());
                int y1 = Integer.parseInt(y1Field.getText());
                int x2 = Integer.parseInt(x2Field.getText());
                int y2 = Integer.parseInt(y2Field.getText());

                // Validate x and y ranges for walls
                if (x1 < 0 || x1 > 1280 || x2 < 0 || x2 > 1280 || y1 < 0 || y1 > 720 || y2 < 0 || y2 > 720) {
                    throw new IllegalArgumentException("X must be between 0 and 1280, Y must be between 0 and 720.");
                }

                canvas.addWall(new Wall(x1, y1, x2, y2));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "Invalid input for wall coordinates. Please enter valid integers.");
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(panel, ex.getMessage());
            }
        });
        addWallPanel.add(addWallButton);
        JButton addRandomWallButton = new JButton("Add Random Wall");
        addRandomWallButton.addActionListener(e -> {
            Point start = new Point(getRandomIntInRange(0, 1280), getRandomIntInRange(0, 720));
            Point end = new Point(getRandomIntInRange(0, 1280), getRandomIntInRange(0, 720));

            canvas.addWall(new Wall(start.x, start.y, end.x, end.y));
        });
        addWallPanel.add(addRandomWallButton);
        panel.add(addWallPanel);
    }

    /**
     * Helper method to add a labeled text field to a panel.
     *
     * @param panel         The panel to which the label and text field will be added.
     * @param labelText     The text for the label.
     * @param textFieldText The initial text for the text field (placeholder).
     * @return The created JTextField for further manipulation or data retrieval.
     */
    private static JTextField addLabeledTextField(JPanel panel, String labelText, String textFieldText) {
        JPanel fieldPanel = new JPanel();
        fieldPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel label = new JLabel(labelText);
        JTextField textField = new JTextField(10); // Adjust size as needed
        textField.setText(textFieldText);
        fieldPanel.add(label);
        fieldPanel.add(textField);
        panel.add(fieldPanel);
        return textField;
    }

}