/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cardcollector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JOptionPane;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import net.proteanit.sql.DbUtils;

/**
 *
 * @author ywkok
 */
public class seller extends javax.swing.JFrame {

    /**
     * Creates new form seller
     */
    public seller() {
        initComponents();
        SelectProduct();
        DisplayTotal();
        Checkout_Confirmbtn.setEnabled(false);
        GetCategory();
    }
    Connection Con = null;
    Statement St = null;
    ResultSet Rs = null;
    
    public void SelectProduct()
    {
        try{
            Con = DriverManager.getConnection("jdbc:derby://localhost:1527/CardCollectordb","User1","user1");
            St = Con.createStatement();
            Rs = St.executeQuery("Select PROID,PROCATEGORY as category,PRONAME as setname,PROSALE as price,PROQUANTITY AS quantity from User1.PRODUCTTB where PROQUANTITY>0");
            POS_ProTable.setModel(DbUtils.resultSetToTableModel(Rs));
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public void DisplayTotal()
    {
        double sum = 0;
        for(int i=0;i<POS_CartTable.getRowCount();i++)
        {
            sum += Integer.parseInt(POS_CartTable.getValueAt(i,3).toString());
        }
        POS_TOTAL.setText(Double.toString(sum));
        Checkout_Total.setText(Double.toString(sum));
        Popup_cashtotal.setText(Double.toString(sum));
    }
    public void DisplayDate()
    {
        
        Date d = new Date();
        SimpleDateFormat s = new SimpleDateFormat("dd-MM-yyyy");
        Checkout_date.setText(s.format(d));
    }
    
    public void DisplaySeller(String id,String name)
    {
        SellerID.setText(id);
        SellerName.setText(name);
    }
    
    public void sales()
    {
        try{
            Con = DriverManager.getConnection("jdbc:derby://localhost:1527/CardCollectordb","User1","user1");
            String query = "insert into SALETB(SELID,TOTALSALE,SALEDATE) values(?,?,?)";
            PreparedStatement add = Con.prepareStatement(query);
            SimpleDateFormat s = new SimpleDateFormat("dd-MM-yyyy");
            Date date = s.parse(Checkout_date.getText());
            java.sql.Date sqlDate = new java.sql.Date(date.getTime());
            add.setInt(1, Integer.parseInt(SellerID.getText()));
            add.setDouble(2, Double.parseDouble(Checkout_Total.getText()));
            add.setDate(3, sqlDate);
            add.executeUpdate();
            
            int rows = Checkout_CartTable.getRowCount();
        
            int product_id = 0;
            String sale_id = "";
            int qty = 0;
            int price = 0;
            int total = 0;
            
            Rs = St.executeQuery("select SALEID FROM SALETB");
            while(Rs.next())
            {
                sale_id = Rs.getString(1);
            }

            String query2 = "insert into SALEPROTB(PROID,SALEID,PRICE,QTY,TOTALPRICE)VALUES(?,?,?,?,?)";
            add = Con.prepareStatement(query2);
            for(int i = 0 ;i<rows;i++)
            {
                product_id = (Integer)Checkout_CartTable.getValueAt(i,0);
                qty = (Integer)Checkout_CartTable.getValueAt(i,2);
                price = (Integer)Checkout_CartTable.getValueAt(i,3);
                total = (Integer)Checkout_CartTable.getValueAt(i,4);
                
                add.setInt(1, product_id);
                add.setString(2,sale_id);
                add.setInt(3,qty);
                add.setInt(4, price);
                add.setInt(5, total);
                add.executeUpdate();
            }   
            String query3 = "update PRODUCTTB set PROQUANTITY = PROQUANTITY-? WHERE PROID=?";
            add = Con.prepareStatement(query3);
            for(int i = 0 ;i<rows;i++)
            {
                product_id = (Integer)Checkout_CartTable.getValueAt(i,0);
                qty = (Integer)Checkout_CartTable.getValueAt(i, 3);
                add.setInt(1, qty);
                add.setInt(2, product_id);
                add.executeUpdate();
            }  
            
            
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        

    }
    public void refreshPaymentMethod()
    {
        Checkout_Confirmbtn.setEnabled(false);
        Checkout_Visabtn.setEnabled(true);
        Checkout_Mastercardbtn.setEnabled(true);
        Checkout_Tngbtn.setEnabled(true);
        Checkout_Cashbtn.setEnabled(true);
        Checkout_Visabtn.setSelected(false);
        Checkout_Mastercardbtn.setSelected(false);
        Checkout_Tngbtn.setSelected(false);
        Checkout_Cashbtn.setSelected(false);
    }
    
    private void GetCategory()
    {
        
        try{
            Con = DriverManager.getConnection("jdbc:derby://localhost:1527/CardCollectordb","User1","user1");
            St = Con.createStatement();
            Rs = St.executeQuery("Select * from User1.CATEGORYTB");
            
            while(Rs.next())
            {
                String MyCat = Rs.getString(2);
                Filterbox.addItem(MyCat);
            }
            
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private void filter(String query)
    {
        DefaultTableModel model = (DefaultTableModel)POS_ProTable.getModel();
        
        TableRowSorter<DefaultTableModel> tr = new TableRowSorter<DefaultTableModel>(model);
        POS_ProTable.setRowSorter(tr);
        
        if(query!= "ALL")
        {
            tr.setRowFilter(RowFilter.regexFilter(query));
        }else
        {
            POS_ProTable.setRowSorter(tr);
        }
    }
    
    public void GetReceipt()
    {
        String sale_id ="";
        try
        {
            Con = DriverManager.getConnection("jdbc:derby://localhost:1527/CardCollectordb","User1","user1");
            Rs = St.executeQuery("select SALEID FROM SALETB");
            while(Rs.next())
            {
                sale_id = Rs.getString(1);
            }
        }
        catch(Exception e)
        {
            e.getStackTrace();
        }
        
        if(Checkout_Cashbtn.isSelected())
        {
            double balance = Double.parseDouble(Popup_cashpay.getText())-Double.parseDouble(Popup_cashtotal.getText());
            
            receipt.setText("	               Welcome to\n" +"                            Card Collecetion Hobby Store\n"+ 
                            "\n      Sales ID :" + sale_id +"\t\t                           "+ Checkout_date.getText() +
                            "\n      Cashier :" + SellerName.getText() + "\n\n      ============================================\n\n"+
                            "      Qty\tName\tPrice\tAmount\n"+Receipt()+"\n\t\t\t======\n"+
                            "      \t\tTOTAL\tRM"+Checkout_Total.getText()+
                            "      \n\t\tCash\tRM"+Popup_cashpay.getText()+
                            "      \n\n\t\tBalance\tRM"+String.format("%.2f",balance)+
                            "\n      ============================================\n"+"	      Thanks For Purchase\n" +"	         See You Soon ~");
        }else
        {
            receipt.setText("	               Welcome to\n" +"                            Card Collecetion Hobby Store\n"+ 
                            "\n      Sales ID :" + sale_id +"\t\t                           "+ Checkout_date.getText() +
                            "\n      Cashier :" + SellerName.getText() + "\n\n      ============================================\n\n"+
                            "      Qty\tName\tPrice\tAmount\n"+Receipt()+"\n\t\t\t======\n"+
                            "      \t\tTOTAL\tRM"+Checkout_Total.getText()+
                            "\n      ============================================\n"+"	      Thanks For Purchase\n" +"	         See You Soon ~");
        }
        
    }
    
    public String Receipt()
    {
        DefaultTableModel model = (DefaultTableModel)Checkout_CartTable.getModel();
        int rows = Checkout_CartTable.getRowCount();
        String result = "";
        for(int i = 0 ;i<rows;i++)
            {
                String Quantity = model.getValueAt(i,3).toString();
                String Name = model.getValueAt(i,1).toString();
                String Price = model.getValueAt(i,2).toString();; 
                String Amount = model.getValueAt(i,4).toString();;
                result += "      " + Quantity + "\t" + Name + "\tRM" + Price + "\tRM" + Amount +"\n";               
            }  
        return result;
    }
    
    public void startNewSales()
    {
        DefaultTableModel model = (DefaultTableModel)POS_CartTable.getModel();
        DefaultTableModel model2 = (DefaultTableModel)Checkout_CartTable.getModel();
        model.setRowCount(0);
        model2.setRowCount(0);
        DisplayTotal();
        SelectProduct();
        Popup_cash.setVisible(false);
        Popup_receipt.setVisible(false);
        Popup_checkout.setVisible(false);
        refreshPaymentMethod();  
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        Popup_checkout = new javax.swing.JDialog();
        jPanel5 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        Checkout_Total = new javax.swing.JLabel();
        Checkout_date = new javax.swing.JLabel();
        JLabel13 = new javax.swing.JLabel();
        JLable14 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        Checkout_CartTable = new javax.swing.JTable();
        jPanel7 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        Checkout_Backbtn = new javax.swing.JButton();
        Checkout_Confirmbtn = new javax.swing.JButton();
        Checkout_Visabtn = new javax.swing.JToggleButton();
        Checkout_Mastercardbtn = new javax.swing.JToggleButton();
        Checkout_Tngbtn = new javax.swing.JToggleButton();
        Checkout_Cashbtn = new javax.swing.JToggleButton();
        Popup_cash = new javax.swing.JDialog();
        jPanel8 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        Popup_cashpay = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        Popup_cashtotal = new javax.swing.JLabel();
        Popup_cashConfirm = new javax.swing.JButton();
        Popup_receipt = new javax.swing.JDialog();
        jPanel9 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        receipt = new javax.swing.JTextArea();
        Popup_receiptPrint = new javax.swing.JButton();
        Popup_receiptSkip = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jlabel19 = new javax.swing.JLabel();
        SellerID = new javax.swing.JLabel();
        jlabel18 = new javax.swing.JLabel();
        SellerName = new javax.swing.JLabel();
        Filterbox = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        POS_CartTable = new javax.swing.JTable();
        jLabel9 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        POS_ProTable = new javax.swing.JTable();
        POS_PROID = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        POS_Addbtn = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        POS_Checkoutbtn = new javax.swing.JButton();
        POS_TOTAL = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        POS_Deletebtn = new javax.swing.JButton();
        POS_PROQuantity = new javax.swing.JSpinner();
        Logout = new javax.swing.JLabel();

        Popup_checkout.setUndecorated(true);

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));
        jPanel5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jPanel6.setBackground(new java.awt.Color(50, 151, 90));

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("CHECK OUT");

        Checkout_Total.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        Checkout_Total.setForeground(new java.awt.Color(255, 255, 255));

        Checkout_date.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        Checkout_date.setForeground(new java.awt.Color(255, 255, 255));

        JLabel13.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        JLabel13.setForeground(new java.awt.Color(255, 255, 255));
        JLabel13.setText("DATE   : ");

        JLable14.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        JLable14.setForeground(new java.awt.Color(255, 255, 255));
        JLable14.setText("TOTAL :   RM");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                        .addComponent(JLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(Checkout_date, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(86, 86, 86))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(JLable14)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Checkout_Total, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(45, 45, 45)
                        .addComponent(jLabel2))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(JLable14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(Checkout_Total, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(JLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(Checkout_date, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(26, Short.MAX_VALUE))
        );

        jScrollPane3.setBackground(new java.awt.Color(50, 151, 90));
        jScrollPane3.setForeground(new java.awt.Color(50, 151, 90));

        Checkout_CartTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Product ID", "Set Name", "Price", "Quantity", "Total Price"
            }
        ));
        Checkout_CartTable.setIntercellSpacing(new java.awt.Dimension(0, 0));
        Checkout_CartTable.setRowHeight(25);
        Checkout_CartTable.setSelectionBackground(new java.awt.Color(55, 151, 90));
        Checkout_CartTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane3.setViewportView(Checkout_CartTable);
        if (Checkout_CartTable.getColumnModel().getColumnCount() > 0) {
            Checkout_CartTable.getColumnModel().getColumn(2).setResizable(false);
        }

        jPanel7.setBackground(new java.awt.Color(50, 151, 90));

        jLabel15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pic/PinClipart.com_clipart-spa_4035278.png"))); // NOI18N

        jLabel16.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel16.setForeground(new java.awt.Color(255, 255, 255));
        jLabel16.setText("Your Order");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel16)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel16)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel17.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel17.setText("Payment Method");

        Checkout_Backbtn.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        Checkout_Backbtn.setText("<- BACK");
        Checkout_Backbtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Checkout_BackbtnActionPerformed(evt);
            }
        });

        Checkout_Confirmbtn.setBackground(new java.awt.Color(50, 151, 90));
        Checkout_Confirmbtn.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        Checkout_Confirmbtn.setText("CONFIRM");
        Checkout_Confirmbtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Checkout_ConfirmbtnActionPerformed(evt);
            }
        });

        Checkout_Visabtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pic/VISA.png"))); // NOI18N
        Checkout_Visabtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Checkout_VisabtnActionPerformed(evt);
            }
        });

        Checkout_Mastercardbtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pic/BANK.png"))); // NOI18N
        Checkout_Mastercardbtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Checkout_MastercardbtnActionPerformed(evt);
            }
        });

        Checkout_Tngbtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pic/TNG.png"))); // NOI18N
        Checkout_Tngbtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Checkout_TngbtnActionPerformed(evt);
            }
        });

        Checkout_Cashbtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pic/CASH.png"))); // NOI18N
        Checkout_Cashbtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Checkout_CashbtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(Checkout_Backbtn, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 353, Short.MAX_VALUE)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel17)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(Checkout_Tngbtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(Checkout_Visabtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(Checkout_Mastercardbtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(Checkout_Cashbtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addComponent(Checkout_Confirmbtn, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(15, 15, 15))))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(35, 35, 35)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel17)
                    .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(Checkout_Visabtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(Checkout_Mastercardbtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(Checkout_Tngbtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(Checkout_Cashbtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 48, Short.MAX_VALUE)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(Checkout_Confirmbtn, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Checkout_Backbtn))
                .addGap(21, 21, 21))
        );

        javax.swing.GroupLayout Popup_checkoutLayout = new javax.swing.GroupLayout(Popup_checkout.getContentPane());
        Popup_checkout.getContentPane().setLayout(Popup_checkoutLayout);
        Popup_checkoutLayout.setHorizontalGroup(
            Popup_checkoutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        Popup_checkoutLayout.setVerticalGroup(
            Popup_checkoutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        Popup_cash.setUndecorated(true);

        jPanel8.setBackground(new java.awt.Color(255, 255, 255));

        jLabel13.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel13.setText("Total Price : ");

        Popup_cashpay.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N

        jLabel18.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel18.setText("Pay : ");

        Popup_cashtotal.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N

        Popup_cashConfirm.setBackground(new java.awt.Color(50, 151, 90));
        Popup_cashConfirm.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        Popup_cashConfirm.setText("CONFIRM");
        Popup_cashConfirm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Popup_cashConfirmActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel13)
                            .addComponent(jLabel18))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(Popup_cashpay, javax.swing.GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE)
                            .addComponent(Popup_cashtotal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(40, 40, 40)
                        .addComponent(Popup_cashConfirm)))
                .addContainerGap(43, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(Popup_cashtotal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(Popup_cashpay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                .addComponent(Popup_cashConfirm)
                .addContainerGap())
        );

        javax.swing.GroupLayout Popup_cashLayout = new javax.swing.GroupLayout(Popup_cash.getContentPane());
        Popup_cash.getContentPane().setLayout(Popup_cashLayout);
        Popup_cashLayout.setHorizontalGroup(
            Popup_cashLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        Popup_cashLayout.setVerticalGroup(
            Popup_cashLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        Popup_receipt.setUndecorated(true);

        jPanel9.setBackground(new java.awt.Color(255, 255, 255));

        receipt.setColumns(20);
        receipt.setRows(5);
        jScrollPane4.setViewportView(receipt);

        Popup_receiptPrint.setBackground(new java.awt.Color(50, 151, 90));
        Popup_receiptPrint.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        Popup_receiptPrint.setText("Print");
        Popup_receiptPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Popup_receiptPrintActionPerformed(evt);
            }
        });

        Popup_receiptSkip.setBackground(new java.awt.Color(50, 151, 90));
        Popup_receiptSkip.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        Popup_receiptSkip.setText("Skip");
        Popup_receiptSkip.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Popup_receiptSkipActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(82, 82, 82)
                .addComponent(Popup_receiptPrint)
                .addGap(48, 48, 48)
                .addComponent(Popup_receiptSkip)
                .addContainerGap(92, Short.MAX_VALUE))
            .addComponent(jScrollPane4)
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 389, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 13, Short.MAX_VALUE)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Popup_receiptPrint)
                    .addComponent(Popup_receiptSkip))
                .addContainerGap())
        );

        javax.swing.GroupLayout Popup_receiptLayout = new javax.swing.GroupLayout(Popup_receipt.getContentPane());
        Popup_receipt.getContentPane().setLayout(Popup_receiptLayout);
        Popup_receiptLayout.setHorizontalGroup(
            Popup_receiptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        Popup_receiptLayout.setVerticalGroup(
            Popup_receiptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel1.setPreferredSize(new java.awt.Dimension(900, 603));

        jPanel2.setBackground(new java.awt.Color(0, 0, 0));

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pic/cards60px.png"))); // NOI18N

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(50, 151, 95));
        jLabel4.setText("Card Collection ");

        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(50, 151, 95));
        jLabel5.setText("Hobby Store");

        jLabel6.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(50, 151, 95));
        jLabel6.setText("Sale Billing Point");

        jlabel19.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jlabel19.setForeground(new java.awt.Color(50, 151, 90));
        jlabel19.setText("Seller Name :");

        SellerID.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        SellerID.setForeground(new java.awt.Color(50, 151, 90));

        jlabel18.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jlabel18.setForeground(new java.awt.Color(50, 151, 90));
        jlabel18.setText("Seller ID     :");

        SellerName.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        SellerName.setForeground(new java.awt.Color(50, 151, 90));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 131, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addGap(109, 109, 109)
                        .addComponent(jlabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jlabel18, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(SellerID, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SellerName, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(80, 80, 80))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(34, 34, 34)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(SellerID, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jlabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jlabel19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(SellerName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel6)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel4))
                            .addComponent(jLabel3))))
                .addContainerGap(20, Short.MAX_VALUE))
        );

        Filterbox.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        Filterbox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "ALL" }));
        Filterbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FilterboxActionPerformed(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel8.setText("Categories Filter");

        jScrollPane1.setBackground(new java.awt.Color(50, 151, 90));
        jScrollPane1.setForeground(new java.awt.Color(50, 151, 90));

        POS_CartTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ProductID", "Price", "Quantity", "Total Price"
            }
        ));
        POS_CartTable.setIntercellSpacing(new java.awt.Dimension(0, 0));
        POS_CartTable.setRowHeight(25);
        POS_CartTable.setSelectionBackground(new java.awt.Color(55, 151, 90));
        POS_CartTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(POS_CartTable);

        jLabel9.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel9.setText("CART LIST");

        jScrollPane2.setBackground(new java.awt.Color(50, 151, 90));
        jScrollPane2.setForeground(new java.awt.Color(50, 151, 90));

        POS_ProTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Product ID", "Category", "Set Name", "Price", "Quantity"
            }
        ));
        POS_ProTable.setIntercellSpacing(new java.awt.Dimension(0, 0));
        POS_ProTable.setRowHeight(25);
        POS_ProTable.setSelectionBackground(new java.awt.Color(55, 151, 90));
        POS_ProTable.getTableHeader().setReorderingAllowed(false);
        POS_ProTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                POS_ProTableMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(POS_ProTable);

        POS_PROID.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        POS_PROID.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                POS_PROIDActionPerformed(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel7.setText("Product ID");

        jLabel10.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel10.setText("Quantity");

        POS_Addbtn.setBackground(new java.awt.Color(255, 255, 255));
        POS_Addbtn.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        POS_Addbtn.setText("Add to Cart");
        POS_Addbtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                POS_AddbtnActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel1.setText("SUB TOTAL      RM ");

        POS_Checkoutbtn.setBackground(new java.awt.Color(50, 151, 90));
        POS_Checkoutbtn.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        POS_Checkoutbtn.setForeground(new java.awt.Color(255, 255, 255));
        POS_Checkoutbtn.setText("Check out");
        POS_Checkoutbtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                POS_CheckoutbtnActionPerformed(evt);
            }
        });

        POS_TOTAL.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        POS_TOTAL.setText("               ");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(POS_TOTAL)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(POS_Checkoutbtn)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(POS_Checkoutbtn)
                    .addComponent(POS_TOTAL))
                .addContainerGap())
        );

        jPanel4.setBackground(new java.awt.Color(50, 151, 90));

        jLabel11.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(255, 255, 255));
        jLabel11.setText("Cart List");

        jLabel12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pic/PinClipart.com_clipart-spa_4035278.png"))); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel11)
                .addContainerGap(261, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(15, Short.MAX_VALUE)
                .addComponent(jLabel11)
                .addContainerGap())
        );

        POS_Deletebtn.setBackground(new java.awt.Color(255, 255, 255));
        POS_Deletebtn.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        POS_Deletebtn.setText("Delete");
        POS_Deletebtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                POS_DeletebtnActionPerformed(evt);
            }
        });

        POS_PROQuantity.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        POS_PROQuantity.setModel(new javax.swing.SpinnerNumberModel(1, 1, null, 1));

        Logout.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        Logout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pic/exit.png"))); // NOI18N
        Logout.setText("Logout");
        Logout.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                LogoutMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(37, 37, 37)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 390, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(22, 22, 22)
                                .addComponent(Logout)))
                        .addGap(151, 151, 151)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(POS_PROID, javax.swing.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE)
                            .addComponent(POS_PROQuantity))
                        .addGap(7, 7, 7))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(45, 45, 45)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(Filterbox, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(331, 331, 331)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(POS_Addbtn)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(POS_Deletebtn, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(jLabel7)
                                            .addComponent(jLabel9)
                                            .addComponent(jLabel10)
                                            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGap(0, 0, Short.MAX_VALUE)))))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31)
                .addComponent(jLabel8)
                .addGap(6, 6, 6)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(Filterbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addComponent(POS_PROID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(56, 56, 56)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(POS_Addbtn)
                                    .addComponent(POS_Deletebtn))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 327, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(29, 29, 29)
                                .addComponent(Logout)
                                .addGap(10, 10, 10))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel7)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel10)
                            .addComponent(POS_PROQuantity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void FilterboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FilterboxActionPerformed
        // TODO add your handling code here:
        String query = Filterbox.getSelectedItem().toString();
        filter (query);
    }//GEN-LAST:event_FilterboxActionPerformed

    private void POS_PROIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_POS_PROIDActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_POS_PROIDActionPerformed

    private void POS_AddbtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_POS_AddbtnActionPerformed
        // TODO add your handling code here:
        if(POS_PROID.getText().isEmpty())
        {
            JOptionPane.showMessageDialog(this,"Select the Product to add to the cart");
        }else
        {
            try{
                
                Con = DriverManager.getConnection("jdbc:derby://localhost:1527/CardCollectordb","User1","user1");
                St = Con.createStatement();
                Rs = St.executeQuery("Select PROID,PRONAME,PROSALE,PROQUANTITY from PRODUCTTB where PROID="+POS_PROID.getText());

                DefaultTableModel model = (DefaultTableModel)POS_CartTable.getModel();
                DefaultTableModel model2 = (DefaultTableModel)Checkout_CartTable.getModel();
                
                int rows = POS_CartTable.getRowCount();
                int cartQty=0;
                for(int i=0;i<rows;i++)
                {
                    if((Integer)POS_CartTable.getValueAt(i, 0) == Integer.parseInt(POS_PROID.getText()))
                    {
                        cartQty += (Integer)POS_CartTable.getValueAt(i, 2);
                    }
                }
                if(Rs.next())
                {
                        
                    int ProductID = Rs.getInt(1);
                    String SetName = Rs.getString(2);
                    int Price = Rs.getInt(3);
                    int Quantity = (Integer)POS_PROQuantity.getValue();
                    cartQty += Quantity;
                    int availableQty = Rs.getInt(4);
                    int TotalPrice = Quantity * Price;
                    if(Quantity>availableQty || cartQty >availableQty)
                    {
                        JOptionPane.showMessageDialog(this,"The available product quantity : "+availableQty);
                    }else
                    {
                        model.addRow(new Object[]{ProductID,Price,Quantity,TotalPrice});
                        model2.addRow(new Object[]{ProductID,SetName,Price,Quantity,TotalPrice});
                        
                        POS_PROID.setText("");
                     }
                }else
                {
                    JOptionPane.showMessageDialog(this,"The Selected Product doesn't exist");
                }
                DisplayTotal();
                
            }catch(Exception e)
            {
                e.printStackTrace();
            }
            
        }
    }//GEN-LAST:event_POS_AddbtnActionPerformed

    private void POS_CheckoutbtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_POS_CheckoutbtnActionPerformed
        // TODO add your handling code here:
        double total = Double.parseDouble(POS_TOTAL.getText());
        if(total > 0)
        {
            Popup_checkout.pack();
            Popup_checkout.setLocationRelativeTo(null);
            DisplayDate();
            Popup_checkout.setVisible(true);
        }else
        {
            JOptionPane.showMessageDialog(this,"There is no product to be purchase");
        }
    }//GEN-LAST:event_POS_CheckoutbtnActionPerformed

    private void POS_DeletebtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_POS_DeletebtnActionPerformed
        // TODO add your handling code here:
        
        DefaultTableModel model = (DefaultTableModel)POS_CartTable.getModel();
        DefaultTableModel model2 = (DefaultTableModel)Checkout_CartTable.getModel();
        int Myindex = POS_CartTable.getSelectedRow();
        if(Myindex == -1)
        {
            JOptionPane.showMessageDialog(this,"Select the Product to be delete");
        }else
        {
            model.removeRow(Myindex);
            model2.removeRow(Myindex);
        }
        DisplayTotal();
        
    }//GEN-LAST:event_POS_DeletebtnActionPerformed

    private void POS_ProTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_POS_ProTableMouseClicked
        // TODO add your handling code here:
        DefaultTableModel model = (DefaultTableModel)POS_ProTable.getModel();
        int Myindex = POS_ProTable.getSelectedRow();
        POS_PROID.setText(model.getValueAt(Myindex,0).toString());
    }//GEN-LAST:event_POS_ProTableMouseClicked

    private void Checkout_BackbtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Checkout_BackbtnActionPerformed
        // TODO add your handling code here:
        Popup_checkout.setVisible(false);
    }//GEN-LAST:event_Checkout_BackbtnActionPerformed

    private void Checkout_ConfirmbtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Checkout_ConfirmbtnActionPerformed
        // TODO add your handling code here:
        sales();
        if(Checkout_Cashbtn.isSelected())
        {
            Popup_cash.pack();
            Popup_cash.setLocationRelativeTo(null);
            Popup_cash.setVisible(true);
        }else
        {
            GetReceipt();
            Popup_receipt.pack();
            Popup_receipt.setLocationRelativeTo(null);
            Popup_receipt.setVisible(true);
        }
        
    }//GEN-LAST:event_Checkout_ConfirmbtnActionPerformed

    private void Checkout_VisabtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Checkout_VisabtnActionPerformed
        // TODO add your handling code here:
        if(Checkout_Visabtn.isSelected()){
            Checkout_Confirmbtn.setEnabled(true);
            Checkout_Mastercardbtn.setEnabled(false);
            Checkout_Tngbtn.setEnabled(false);
            Checkout_Cashbtn.setEnabled(false);
        }
        else{
            Checkout_Confirmbtn.setEnabled(false);
            Checkout_Mastercardbtn.setEnabled(true);
            Checkout_Tngbtn.setEnabled(true);
            Checkout_Cashbtn.setEnabled(true);
        }
    }//GEN-LAST:event_Checkout_VisabtnActionPerformed

    private void Checkout_MastercardbtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Checkout_MastercardbtnActionPerformed
        // TODO add your handling code here:
        if(Checkout_Mastercardbtn.isSelected()){
            Checkout_Confirmbtn.setEnabled(true);
            Checkout_Visabtn.setEnabled(false);
            Checkout_Tngbtn.setEnabled(false);
            Checkout_Cashbtn.setEnabled(false);
        }
        else{
            Checkout_Confirmbtn.setEnabled(false);
            Checkout_Visabtn.setEnabled(true);
            Checkout_Tngbtn.setEnabled(true);
            Checkout_Cashbtn.setEnabled(true);
        }
    }//GEN-LAST:event_Checkout_MastercardbtnActionPerformed

    private void Checkout_TngbtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Checkout_TngbtnActionPerformed
        // TODO add your handling code here:
        if(Checkout_Tngbtn.isSelected()){
            Checkout_Confirmbtn.setEnabled(true);
            Checkout_Visabtn.setEnabled(false);
            Checkout_Mastercardbtn.setEnabled(false);
            Checkout_Cashbtn.setEnabled(false);
        }
        else{
            Checkout_Confirmbtn.setEnabled(false);
            Checkout_Visabtn.setEnabled(true);
            Checkout_Mastercardbtn.setEnabled(true);
            Checkout_Cashbtn.setEnabled(true);
        }
    }//GEN-LAST:event_Checkout_TngbtnActionPerformed

    private void Checkout_CashbtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Checkout_CashbtnActionPerformed
        // TODO add your handling code here:
        if(Checkout_Cashbtn.isSelected()){
            Checkout_Confirmbtn.setEnabled(true);
            Checkout_Visabtn.setEnabled(false);
            Checkout_Mastercardbtn.setEnabled(false);
            Checkout_Tngbtn.setEnabled(false);
        }
        else{
            Checkout_Confirmbtn.setEnabled(false);
            Checkout_Visabtn.setEnabled(true);
            Checkout_Mastercardbtn.setEnabled(true);
            Checkout_Tngbtn.setEnabled(true);
        }
    }//GEN-LAST:event_Checkout_CashbtnActionPerformed

    private void LogoutMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_LogoutMouseClicked
        // TODO add your handling code here:
        reg.loginform1.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_LogoutMouseClicked

    private void Popup_cashConfirmActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Popup_cashConfirmActionPerformed
        // TODO add your handling code here:
        if(Double.parseDouble(Popup_cashpay.getText())>=Double.parseDouble(Popup_cashtotal.getText()))
        {
            GetReceipt();
            Popup_receipt.pack();
            Popup_receipt.setLocationRelativeTo(null);
            Popup_receipt.setVisible(true);
        }else
        {
            JOptionPane.showMessageDialog(this,"Payment amount is less than the total price");
        }

    }//GEN-LAST:event_Popup_cashConfirmActionPerformed

    private void Popup_receiptSkipActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Popup_receiptSkipActionPerformed
        // TODO add your handling code here:
        startNewSales();
        Popup_cashpay.setText("");
    }//GEN-LAST:event_Popup_receiptSkipActionPerformed

    private void Popup_receiptPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Popup_receiptPrintActionPerformed
        // TODO add your handling code here:
        JOptionPane.showMessageDialog(this,"Done printing");
        startNewSales();
        Popup_cashpay.setText("");
    }//GEN-LAST:event_Popup_receiptPrintActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(seller.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(seller.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(seller.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(seller.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new seller().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Checkout_Backbtn;
    private javax.swing.JTable Checkout_CartTable;
    private javax.swing.JToggleButton Checkout_Cashbtn;
    private javax.swing.JButton Checkout_Confirmbtn;
    private javax.swing.JToggleButton Checkout_Mastercardbtn;
    private javax.swing.JToggleButton Checkout_Tngbtn;
    private javax.swing.JLabel Checkout_Total;
    private javax.swing.JToggleButton Checkout_Visabtn;
    private javax.swing.JLabel Checkout_date;
    private javax.swing.JComboBox<String> Filterbox;
    private javax.swing.JLabel JLabel13;
    private javax.swing.JLabel JLable14;
    private javax.swing.JLabel Logout;
    private javax.swing.JButton POS_Addbtn;
    private javax.swing.JTable POS_CartTable;
    private javax.swing.JButton POS_Checkoutbtn;
    private javax.swing.JButton POS_Deletebtn;
    private javax.swing.JTextField POS_PROID;
    private javax.swing.JSpinner POS_PROQuantity;
    private javax.swing.JTable POS_ProTable;
    private javax.swing.JLabel POS_TOTAL;
    private javax.swing.JDialog Popup_cash;
    private javax.swing.JButton Popup_cashConfirm;
    private javax.swing.JTextField Popup_cashpay;
    private javax.swing.JLabel Popup_cashtotal;
    private javax.swing.JDialog Popup_checkout;
    private javax.swing.JDialog Popup_receipt;
    private javax.swing.JButton Popup_receiptPrint;
    private javax.swing.JButton Popup_receiptSkip;
    private javax.swing.JLabel SellerID;
    private javax.swing.JLabel SellerName;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JLabel jlabel18;
    private javax.swing.JLabel jlabel19;
    private javax.swing.JTextArea receipt;
    // End of variables declaration//GEN-END:variables
}
