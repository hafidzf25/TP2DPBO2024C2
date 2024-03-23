import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Menu extends JFrame{
    public static void main(String[] args) {
        // buat object window
        Menu window = new Menu();

        // atur ukuran window
        window.setSize(480, 560);

        // letakkan window di tengah layar
        window.setLocationRelativeTo(null);

        // isi window
        window.setContentPane(window.mainPanel);

        // ubah warna background
        window.getContentPane().setBackground(Color.white);

        // tampilkan window
        window.setVisible(true);

        // agar program ikut berhenti saat window diclose
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    // index baris yang diklik
    private int selectedIndex = -1;
    // list untuk menampung semua mahasiswa
    private ArrayList<Mahasiswa> listMahasiswa;
    private Database database;
    private JPanel mainPanel;
    private JTextField nimField;
    private JTextField namaField;
    private JTable mahasiswaTable;
    private JButton addUpdateButton;
    private JButton cancelButton;
    private JComboBox jenisKelaminComboBox;
    private JButton deleteButton;
    private JLabel titleLabel;
    private JLabel nimLabel;
    private JLabel namaLabel;
    private JLabel jenisKelaminLabel;
    private JComboBox ptIbuComboBox;
    private JComboBox ptAyahComboBox;
    private JLabel ptIbuLabel;
    private JLabel ptAyahLabel;

    // constructor
    public Menu() {
        // inisialisasi listMahasiswa
        listMahasiswa = new ArrayList<>();

        // buat object database
        database = new Database();

        // isi tabel mahasiswa
        mahasiswaTable.setModel(setTable());

        // ubah styling title
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 20f));

        // atur isi combo box
        String[] jeniskelaminData = {"", "Laki-laki", "Perempuan"};
        jenisKelaminComboBox.setModel(new DefaultComboBoxModel(jeniskelaminData));

        String[] ptData = {"", "Tidak Tamat SD", "SD", "SMP", "SMA/Sederajat", "Diploma", "S1", "S2", "S3"};
        ptIbuComboBox.setModel(new DefaultComboBoxModel(ptData));
        ptAyahComboBox.setModel(new DefaultComboBoxModel(ptData));

        // sembunyikan button delete
        deleteButton.setVisible(false);

        // saat tombol add/update ditekan
        addUpdateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedIndex == - 1) {
                    insertData();
                }
                else {
                    updateData();
                }
            }
        });
        // saat tombol delete ditekan
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedIndex >= 0) {
                    JFrame frame = new JFrame("Konfirmasi");
                    int result = JOptionPane.showConfirmDialog(frame,"Hapus data?", "Konfirmasi",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE);
                    if(result == JOptionPane.YES_OPTION){
                        deleteData();
                    }else if (result == JOptionPane.NO_OPTION){
                        System.out.println("Cancel command.");
                    }else {
                        System.out.println("Cancel command.");
                    }
                }
            }
        });
        // saat tombol cancel ditekan
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearForm();
            }
        });
        // saat salah satu baris tabel ditekan
        mahasiswaTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // ubah selectedIndex menjadi baris tabel yang diklik
                selectedIndex = mahasiswaTable.getSelectedRow();

                // simpan value textfield dan combo box
                String selectedNim = mahasiswaTable.getModel().getValueAt(selectedIndex, 1).toString();
                String selectedNama = mahasiswaTable.getModel().getValueAt(selectedIndex, 2).toString();
                String selectedJenisKelamin = mahasiswaTable.getModel().getValueAt(selectedIndex, 3).toString();
                String selectedPTIbu = mahasiswaTable.getModel().getValueAt(selectedIndex, 4).toString();
                String selectedPTAyah = mahasiswaTable.getModel().getValueAt(selectedIndex, 5).toString();

                // ubah isi textfield dan combo box
                nimField.setText(selectedNim);
                namaField.setText(selectedNama);
                jenisKelaminComboBox.setSelectedItem(selectedJenisKelamin);
                ptIbuComboBox.setSelectedItem(selectedPTIbu);
                ptAyahComboBox.setSelectedItem(selectedPTAyah);

                // ubah button "Add" menjadi "Update"
                addUpdateButton.setText("Update");

                // tampilkan button delete
                deleteButton.setVisible(true);
            }
        });
    }

    public final DefaultTableModel setTable() {
        // tentukan kolom tabel
        Object[] column = {"No", "NIM", "Nama", "Jenis Kelamin", "Pendidikan Terakhir Ibu", "Pendidikan Terakhir Ayah"};

        // buat objek tabel dengan kolom yang sudah dibuat
        DefaultTableModel temp = new DefaultTableModel(null, column);

        try {
            ResultSet resultSet = database.selectQuery("SELECT * FROM mahasiswa");

            int i = 0;
            while (resultSet.next()) {
                Object[] row = new Object[6];

                row[0] = i + 1;
                row[1] = resultSet.getString("nim");
                row[2] = resultSet.getString("nama");
                row[3] = resultSet.getString("jenis_kelamin");
                row[4] = resultSet.getString("pt_ibu");
                row[5] = resultSet.getString("pt_ayah");

                temp.addRow(row);
                i++;
            }
        } catch (SQLException e){
            throw new RuntimeException(e);
        }

        return temp; // return juga harus diganti
    }

    public void insertData() {
        // ambil value dari textfield dan combobox
        String nim = nimField.getText();
        String nama = namaField.getText();
        String jenisKelamin = jenisKelaminComboBox.getSelectedItem().toString();
        String ptIbu = ptIbuComboBox.getSelectedItem().toString();
        String ptAyah = ptAyahComboBox.getSelectedItem().toString();

        // Mengecek apakah terdapat input field/box yang belum di isi
        if (!nim.equals("") && !nama.equals("") && !jenisKelamin.equals("") && !ptIbu.equals("") && !ptAyah.equals("")) {
            boolean flag = true; // Deklarasi flag sebagai cek nim
            // Mengecek nim, apakah terdapat di database
            try {
                ResultSet resultSet = database.selectQuery("SELECT * FROM mahasiswa");
                // Pengecekan secara satu persatu baris
                while (resultSet.next()) {
                    if(nim.equals(resultSet.getString("nim"))){ // Apabila ada, maka set flag menjadi false
                        flag = false;
                    }
                }
            } catch (SQLException e){
                throw new RuntimeException(e);
            }

            if (flag == true) { // Apabila flag true, menandakan bahwa tidak ada nim yang sama
                // tambahkan data ke dalam database
                String sql = "INSERT INTO mahasiswa VALUES (null, '" + nim + "', '" + nama + "', '" + jenisKelamin + "', '" + ptIbu + "', '" + ptAyah + "');";
                database.InsertUpdateDeleteQuery(sql);

                // update tabel
                mahasiswaTable.setModel(setTable());

                // bersihkan form
                clearForm();

                // feedback
                System.out.println("Insert berhasil!");
                JOptionPane.showMessageDialog(null, "Data berhasil ditambahkan!");
            } else { // apabila terdapat nim yang sama atau flag false
                System.out.println("Insert gagal! terdapat nim yang sama pada data");
                JOptionPane.showMessageDialog(null, "NIM yang di input sudah ada dalam data!\nSilahkan input kembali dengan nim berbeda!");
            }
        }
        else { // Apabila terdapat input field/box yang kosong
            // feedback
            System.out.println("Insert gagal! terdapat input field/box yang masih kosong");
            JOptionPane.showMessageDialog(null, "Terdapat input field/box yang kosong!");
        }
    }

    public void updateData() {
        // ambil data dari form
        String nim = nimField.getText();
        String nama = namaField.getText();
        String jenisKelamin = jenisKelaminComboBox.getSelectedItem().toString();
        String ptIbu = ptIbuComboBox.getSelectedItem().toString();
        String ptAyah = ptAyahComboBox.getSelectedItem().toString();

        // Mengecek apakah terdapat input field/box yang belum di isi
        if (!nim.equals("") && !nama.equals("") && !jenisKelamin.equals("") && !ptIbu.equals("") && !ptAyah.equals("")) {
            // ubah data mahasiswa di database
            String sql = "UPDATE mahasiswa SET nama = '" + nama + "', jenis_kelamin = '" + jenisKelamin + "', pt_ibu = '" + ptIbu + "', pt_ayah = '" + ptAyah + "' WHERE nim = '" + nim + "';";
            database.InsertUpdateDeleteQuery(sql);

            // update tabel
            mahasiswaTable.setModel(setTable());

            // bersihkan form
            clearForm();

            // feedback
            System.out.println("Update berhasil!");
            JOptionPane.showMessageDialog(null, "Data berhasil dirubah!");
        }
        else { // Apabila terdapat input field/box yang kosong
            // feedback
            System.out.println("Update gagal! terdapat input field/box yang masih kosong");
            JOptionPane.showMessageDialog(null, "Terdapat input field/box yang kosong!");
        }
    }

    public void deleteData() {
        // mengambil kolom nim dari data mahasiswa yang ingin dihapus
        String nim = nimField.getText();

        // hapus data mahasiswa di database dengan berdasarkan nim yg diambil
        String sql = "DELETE FROM mahasiswa WHERE nim = '" + nim + "';";
        database.InsertUpdateDeleteQuery(sql);

        // update tabel
        mahasiswaTable.setModel(setTable());

        // bersihkan form
        clearForm();

        // feedback
        System.out.println("Delete berhasil!");
        JOptionPane.showMessageDialog(null, "Data berhasil dihapus!");
    }

    public void clearForm() {
        // kosongkan semua texfield dan combo box
        nimField.setText("");
        namaField.setText("");
        jenisKelaminComboBox.setSelectedItem("");
        ptIbuComboBox.setSelectedItem("");
        ptAyahComboBox.setSelectedItem("");

        // ubah button "Update" menjadi "Add"
        addUpdateButton.setText("Add");
        // sembunyikan button delete
        deleteButton.setVisible(false);
        // ubah selectedIndex menjadi -1 (tidak ada baris yang dipilih)
        selectedIndex = -1;
    }
}
