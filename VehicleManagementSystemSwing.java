import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.print.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class VehicleManagementSystemSwing extends JFrame {

    private JTable table;
    private DefaultTableModel model;
    private List<Vehicle> vehicles = new ArrayList<>();
    private JTextField searchField;
    private JTextField rateField;
    private JTextField durationField;

    public VehicleManagementSystemSwing() {
        setTitle("Vehicle Management System – By Ayush Singh");
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel header = new JLabel("Vehicle Management System", JLabel.CENTER);
        header.setOpaque(true);
        header.setBackground(new Color(30, 144, 255));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Arial", Font.BOLD, 26));
        header.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        add(header, BorderLayout.NORTH);

        String[] columns = {"ID", "Name", "Type", "Model", "Year", "Status"};
        model = new DefaultTableModel(columns, 0);
        table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel side = new JPanel(new GridLayout(9, 1, 5, 5));
        side.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton addBtn = new JButton("Add Vehicle");
        JButton editBtn = new JButton("Edit Vehicle");
        JButton delBtn = new JButton("Delete Vehicle");
        JButton saveBtn = new JButton("Save");
        JButton loadBtn = new JButton("Load");
        JButton genReceiptBtn = new JButton("Generate Receipt (JPEG)");
        JButton printReceiptBtn = new JButton("Print Receipt");

        side.add(addBtn);
        side.add(editBtn);
        side.add(delBtn);
        side.add(saveBtn);
        side.add(loadBtn);
        side.add(genReceiptBtn);
        side.add(printReceiptBtn);

        add(side, BorderLayout.WEST);

        JPanel bottom = new JPanel(new BorderLayout(10, 0));

        JPanel pricePanel = new JPanel(new GridLayout(1, 4, 6, 0));
        pricePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 0));
        pricePanel.add(new JLabel("Rate:"));
        rateField = new JTextField("50.00", 8);
        durationField = new JTextField("5", 8);
        pricePanel.add(rateField);
        pricePanel.add(new JLabel("Duration:"));
        pricePanel.add(durationField);

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 10));
        searchField = new JTextField();
        JButton searchBtn = new JButton("Search");
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchBtn, BorderLayout.EAST);

        bottom.add(pricePanel, BorderLayout.WEST);
        bottom.add(searchPanel, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> addVehicle());
        editBtn.addActionListener(e -> editVehicle());
        delBtn.addActionListener(e -> deleteVehicle());
        saveBtn.addActionListener(e -> saveVehicles());
        loadBtn.addActionListener(e -> loadVehicles());
        searchBtn.addActionListener(e -> searchVehicle());
        genReceiptBtn.addActionListener(e -> generateReceiptJPEG());
        printReceiptBtn.addActionListener(e -> printReceipt());
    }

    private void addVehicle() {
        Vehicle v = showVehicleDialog(null);
        if (v != null) {
            vehicles.add(v);
            addRow(v);
        }
    }

    private void editVehicle() {
        int i = table.getSelectedRow();
        if (i == -1) {
            JOptionPane.showMessageDialog(this, "Select a vehicle to edit");
            return;
        }
        Vehicle updated = showVehicleDialog(vehicles.get(i));
        if (updated != null) {
            vehicles.set(i, updated);
            model.setValueAt(updated.id, i, 0);
            model.setValueAt(updated.name, i, 1);
            model.setValueAt(updated.type, i, 2);
            model.setValueAt(updated.model, i, 3);
            model.setValueAt(updated.year, i, 4);
            model.setValueAt(updated.status, i, 5);
        }
    }

    private void deleteVehicle() {
        int i = table.getSelectedRow();
        if (i == -1) {
            JOptionPane.showMessageDialog(this, "Select a vehicle to delete");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this vehicle?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            vehicles.remove(i);
            model.removeRow(i);
        }
    }

    private void saveVehicles() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("vehicles.dat"))) {
            oos.writeObject(vehicles);
            JOptionPane.showMessageDialog(this, "Saved Successfully");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error Saving File: " + e.getMessage());
        }
    }

    private void loadVehicles() {
        File file = new File("vehicles.dat");
        if (!file.exists()) {
            JOptionPane.showMessageDialog(this, "No saved data found. Starting fresh.");
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            @SuppressWarnings("unchecked")
            List<Vehicle> loaded = (List<Vehicle>) ois.readObject();
            vehicles.clear();
            vehicles.addAll(loaded);
            model.setRowCount(0);
            for (Vehicle v : vehicles) addRow(v);
            JOptionPane.showMessageDialog(this, "Loaded Successfully");
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Error Loading File: " + e.getMessage());
        }
    }

    private void searchVehicle() {
        String key = searchField.getText().trim().toLowerCase();
        model.setRowCount(0);
        if (key.isEmpty()) {
            for (Vehicle v : vehicles) addRow(v);
            return;
        }
        for (Vehicle v : vehicles) {
            if (v.name.toLowerCase().contains(key) ||
                v.type.toLowerCase().contains(key) ||
                v.status.toLowerCase().contains(key)) {
                addRow(v);
            }
        }
    }

    private Vehicle showVehicleDialog(Vehicle existing) {
        JTextField idf = new JTextField(existing == null ? "" : existing.id);
        JTextField namef = new JTextField(existing == null ? "" : existing.name);
        JTextField typef = new JTextField(existing == null ? "" : existing.type);
        JTextField modelf = new JTextField(existing == null ? "" : existing.model);
        JTextField yearf = new JTextField(existing == null ? "" : String.valueOf(existing.year));

        String[] statusOptions = {"Available", "Rented", "Repair"};
        JComboBox<String> statusBox = new JComboBox<>(statusOptions);
        if (existing != null) statusBox.setSelectedItem(existing.status);

        JPanel panel = new JPanel(new GridLayout(6, 2, 5, 5));
        panel.add(new JLabel("ID:")); panel.add(idf);
        panel.add(new JLabel("Name:")); panel.add(namef);
        panel.add(new JLabel("Type:")); panel.add(typef);
        panel.add(new JLabel("Model:")); panel.add(modelf);
        panel.add(new JLabel("Year:")); panel.add(yearf);
        panel.add(new JLabel("Status:")); panel.add(statusBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Enter Vehicle Details", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                int year = Integer.parseInt(yearf.getText().trim());
                String id = idf.getText().trim();
                String name = namef.getText().trim();
                String type = typef.getText().trim();
                String model = modelf.getText().trim();
                String status = statusBox.getSelectedItem().toString();
                if (id.isEmpty() || name.isEmpty() || type.isEmpty() || model.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "All fields are required!");
                    return null;
                }
                return new Vehicle(id, name, type, model, year, status);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Year must be a valid number.");
            }
        }
        return null;
    }

    private void addRow(Vehicle v) {
        model.addRow(new Object[]{v.id, v.name, v.type, v.model, v.year, v.status});
    }

   

    private ReceiptData buildReceiptDataForSelection() {
        int i = table.getSelectedRow();
        if (i == -1) {
            JOptionPane.showMessageDialog(this, "Select a vehicle first.");
            return null;
        }
        Vehicle v = vehicles.get(i);

        double rate, duration;
        try {
            rate = Double.parseDouble(rateField.getText().trim());
            duration = Double.parseDouble(durationField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter valid numeric Rate and Duration.");
            return null;
        }

        ReceiptData rd = new ReceiptData();
        rd.id = v.id;
        rd.name = v.name;
        rd.type = v.type;
        rd.model = v.model;
        rd.year = v.year;
        rd.status = v.status;
        rd.rate = rate;
        rd.duration = duration;
        rd.total = rate * duration; 
        rd.title = "Vehicle Rental Receipt";
        rd.footer = "Thank you!";
        return rd;
    }

    private void generateReceiptJPEG() {
        ReceiptData rd = buildReceiptDataForSelection();
        if (rd == null) return;

        BufferedImage img = renderReceiptToImage(rd, 800, 600);
        File out = new File("receipt_" + rd.id + ".jpg");
        try {
            ImageIO.write(img, "jpeg", out); 
            JOptionPane.showMessageDialog(this, "Receipt saved: " + out.getAbsolutePath());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to save JPEG: " + e.getMessage());
        }
    }

    private void printReceipt() {
        ReceiptData rd = buildReceiptDataForSelection();
        if (rd == null) return;

        final BufferedImage img = renderReceiptToImage(rd, 800, 600);

        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Vehicle Receipt - " + rd.id);

        job.setPrintable(new Printable() {
            @Override
            public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException {
                if (pageIndex > 0) return NO_SUCH_PAGE;
                Graphics2D g2 = (Graphics2D) g;
                g2.translate(pf.getImageableX(), pf.getImageableY());

                double iw = img.getWidth();
                double ih = img.getHeight();
                double pw = pf.getImageableWidth();
                double ph = pf.getImageableHeight();
                double scale = Math.min(pw / iw, ph / ih);

                g2.scale(scale, scale);
                g2.drawImage(img, 0, 0, null);
                return PAGE_EXISTS;
            }
        });

        boolean ok = job.printDialog(); 
        if (ok) {
            try {
                job.print(); 
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(this, "Print failed: " + e.getMessage());
            }
        }
    }

    private BufferedImage renderReceiptToImage(ReceiptData rd, int width, int height) {
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                           java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        g.setColor(new Color(200, 200, 200));
        g.drawRect(10, 10, width - 20, height - 20);

        int x = 40;
        int y = 60;
        int line = 30;

        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 26));
        g.drawString(rd.title, x, y);
        y += line + 10;

        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.drawString("ID: " + rd.id, x, y); y += line;
        g.drawString("Name: " + rd.name, x, y); y += line;
        g.drawString("Type: " + rd.type, x, y); y += line;
        g.drawString("Model: " + rd.model, x, y); y += line;
        g.drawString("Year: " + rd.year, x, y); y += line;
        g.drawString("Status: " + rd.status, x, y); y += line + 10;

        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString(String.format("Rate: %.2f", rd.rate), x, y); y += line;
        g.drawString(String.format("Duration: %.2f", rd.duration), x, y); y += line;

        g.setFont(new Font("Arial", Font.BOLD, 22));
        g.setColor(new Color(30, 144, 255));
        g.drawString(String.format("Total: %.2f", rd.total), x, y);

        g.setColor(Color.DARK_GRAY);
        g.setFont(new Font("Arial", Font.ITALIC, 16));
        g.drawString(rd.footer, x, height - 50);

        g.dispose();
        return bi;
    }

    public static void main(String[] args) {
       
        VehicleManagementSystemSwing app = new VehicleManagementSystemSwing();
        app.setVisible(true);
    }

    static class ReceiptData {
        String id, name, type, model, status, title, footer;
        int year;
        double rate, duration, total;
    }
}

class Vehicle implements Serializable {
    private static final long serialVersionUID = 1L;
    String id, name, type, model, status;
    int year;

    Vehicle(String id, String name, String type, String model, int year, String status) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.model = model;
        this.year = year;
        this.status = status;
    }
}
