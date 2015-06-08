import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JTextArea;
import javax.swing.JList;
import javax.swing.AbstractListModel;
import javax.swing.JCheckBox;


public class MainGui {

	private JFrame frame;
	private JTextField txtServerUrl;
	static Pattern featureTypes = Pattern.compile("<Name>.*?</Name>",
			Pattern.DOTALL);
	private static JTextPane featureList = new JTextPane();
	private JTextField txtRessourceUrl;
	private JTextField txtLayerName;
	private JCheckBox checkCache = new JCheckBox("Cache Layer");
	private JTextField txtCorrdinateType;
	private static HashMap<String, String> mapper = new HashMap<>();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainGui window = new MainGui();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainGui() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 378);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel lblUrl = new JLabel("WFS-Server-Url");
		lblUrl.setBounds(10, 11, 82, 14);
		frame.getContentPane().add(lblUrl);
		
		txtServerUrl = new JTextField();
		txtServerUrl.setBounds(10, 36, 189, 20);
		frame.getContentPane().add(txtServerUrl);
		txtServerUrl.setColumns(10);
		
		JButton btnParseUrl = new JButton("ParseUrl");
		btnParseUrl.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				parseUrl(txtServerUrl.getText().trim());
			}
		});
		btnParseUrl.setBounds(237, 35, 89, 23);
		frame.getContentPane().add(btnParseUrl);
		
		JLabel lblNewLabel = new JLabel("Found Features");
		lblNewLabel.setBounds(10, 79, 121, 14);
		frame.getContentPane().add(lblNewLabel);
		
		featureList.setBounds(10, 96, 189, 155);
		frame.getContentPane().add(featureList);
		
		JButton btnStore = new JButton("Store Selection");
		btnStore.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				storeLayerInDatabase();
			}
		});
		btnStore.setBounds(237, 290, 105, 23);
		frame.getContentPane().add(btnStore);
		
		JLabel lblNewLabel_1 = new JLabel("Choose Ressource");
		lblNewLabel_1.setBounds(237, 79, 105, 14);
		frame.getContentPane().add(lblNewLabel_1);
		
		txtRessourceUrl = new JTextField();
		txtRessourceUrl.setBounds(237, 96, 187, 20);
		frame.getContentPane().add(txtRessourceUrl);
		txtRessourceUrl.setColumns(10);
		

		checkCache.setBounds(237, 260, 97, 23);
		frame.getContentPane().add(checkCache);
		
		JLabel lblNewLabel_2 = new JLabel("Layer Name");
		lblNewLabel_2.setBounds(237, 139, 82, 14);
		frame.getContentPane().add(lblNewLabel_2);
		
		txtLayerName = new JTextField();
		txtLayerName.setBounds(237, 164, 187, 20);
		frame.getContentPane().add(txtLayerName);
		txtLayerName.setColumns(10);
		
		JLabel lblNewLabel_3 = new JLabel("Coordinate type");
		lblNewLabel_3.setBounds(237, 206, 96, 14);
		frame.getContentPane().add(lblNewLabel_3);
		
		txtCorrdinateType = new JTextField();
		txtCorrdinateType.setBounds(240, 231, 184, 20);
		frame.getContentPane().add(txtCorrdinateType);
		txtCorrdinateType.setColumns(10);
	}
	
	
	
	private void parseUrl(String s){
		try{
		String addurl = "?REQUEST=GetCapabilities&SERVICE=WFS&Version=1.3.0";
		 
		URL obj = new URL(s+addurl);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 
		// optional default is GET
		con.setRequestMethod("GET");
 
		//add request header
		con.setRequestProperty("User-Agent", "Mozilla/5.0");
 
		int responseCode = con.getResponseCode();
		System.out.println("Response Code : " + responseCode);
 
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
 
		String sResponse = response.toString();
		String textResponse = "";
		Pattern getCoordTypePattern = Pattern.compile("<DefaultSRS>.*?</DefaultSRS>",Pattern.DOTALL);
		Matcher featureTypeMatcher = featureTypes.matcher(sResponse);
		while (featureTypeMatcher.find()) {
			String s3 = "";
			String st = featureTypeMatcher.group();
			st = st.replace("<Name>", "");
			Pattern coordTypePattern = Pattern.compile(st+".*?</DefaultSRS>",Pattern.DOTALL);
			Matcher matcher2 = coordTypePattern.matcher(sResponse);
				if(matcher2.find()){
					Matcher matcher3 = getCoordTypePattern.matcher(matcher2.group());
						if(matcher3.find()){
							s3 = matcher3.group();
							s3 = s3.replace("<DefaultSRS>", "");
							s3 = s3.replace("</DefaultSRS>", "");
						}
				}
			st = st.replace("</Name>", "");
			mapper.put(st, s3);
			textResponse += st +"\n";
		}
		
		featureList.setText(textResponse);
		
		}catch (Exception e){
			e.printStackTrace();
		}
 
	}
	
	
	private void storeLayerInDatabase(){
		String res = txtRessourceUrl.getText();
		String url = txtServerUrl.getText();
		String Layername = txtLayerName.getText();
		boolean cache = checkCache.isSelected();
		String type = "WFS";
		String coordtype = txtCorrdinateType.getText();
		
		if(coordtype.equals("")){
		try{
			txtCorrdinateType.setText(mapper.get(res));
			coordtype = mapper.get(res);
		}catch (Exception e){}
		}else{
			
		}
		
		try{
			
			Class.forName("com.mysql.jdbc.Driver");
			Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:1337/test","root","azzlan1992");
			String insertQuery = "INSERT INTO test.customlayer"
					+"(LAYERNAME,URL,RESSOURCE,TYPE,CACHE,COORDTYPE) VALUES"
					+"(?,?,?,?,?,?)";
			PreparedStatement prep = conn.prepareStatement(insertQuery);
			prep.setString(1,Layername );
			prep.setString(2,url );
			prep.setString(3,res );
			prep.setString(4,type );
			if(cache){
				prep.setString(5,"True" );	
			}else{
				prep.setString(5,"False" );
			}
			prep.setString(6, coordtype);
			prep.executeUpdate();
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
}
