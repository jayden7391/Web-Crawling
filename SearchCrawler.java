import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.table.*;

// The Search Web Crawler
public class SearchCrawler extends JFrame {
	// Max URLs drop down values.
	private static final String[] MAX_URLS = { "50", "100", "500", "1000" }; // �޺��ڽ���
	// ǥ����
	// ����
	// ->
	// �ִ�
	// URL
	// ����
	// ����

	// Cache of robot disallow lists.
	private HashMap disallowListCache = new HashMap(); // disallow : ~�� �㰡���� �ʴ�.
	// robot�� ħ���� ������� ���� site�� ����Ʈ

	// Search GUI controls. // run���� �� ������ UI�� ���ؼ� �Ʒ� �������� ���ϸ� �� ��
	// File�̶�� ���ִ� ���� �Ʒ��κ�
	private JTextField startTextField; // startURL�� �ؽ�Ʈ �ʵ�
	private JComboBox maxComboBox; // MaxURLs�� �޺��ڽ� => 50 100 500 1000
	private JCheckBox limitCheckBox; // Limit crawling to start URL site�� üũ�ڽ�
	private JTextField logTextField; // Matches Log File�� �ؽ�Ʈ �ʵ�
	private JTextField searchTextField;// Search String�� �ؽ�Ʈ �ʵ�
	private JCheckBox caseCheckBox; // Case sensitive�� üũ �ڽ�
	private JButton searchButton; // Search ��ư

	// Search stats GUI controls.
	// Search ��ư�� ���� �Ʒ� �κ�
	private JLabel crawlingLabel2; // Crawling :
	private JLabel crawledLabel2; // Crawled URLS :
	private JLabel toCrawlLabel2; // URL to Crawl :
	private JProgressBar progressBar; // Crawling Progress [[[[[[[[ 0%
	// ]]]]]]]]]]
	private JLabel matchesLabel2; // URL
	// Table listing search matches.
	private JTable table; // URL�̶�� ���ִ� ���� ū �ؽ�Ʈ �ڽ� ���� �� -> ���߿� ��� ���

	// Flag for whether or not crawling is underway.
	private boolean crawling; // �κ��� Ȱ���� ���� ������ �ƴ��� ��Ÿ�� (��Ÿ���� ���� Flag��� �ϴ� �� ����)

	// Matches log file print writer.
	private PrintWriter logFileWriter; // �α� ���� ����� ���� ��

	// Constructor for Search Web Crawler. => �����ڿ��� UI�� ���� ��� ���� �̷�� ����. (����
	// Flex�� ��ü)
	public SearchCrawler() {
		// Set application title.
		setTitle("Search Crawler"); // UI�� ����

		// Set window size.
		setSize(600, 600); // width 600 height 700���� ����

		// Handle window closing events.
		addWindowListener(new WindowAdapter() { // UI�� �ݱ� ���� WindowListener ����
			// (X�� ������ �� �����)
			public void windowClosing(WindowEvent e) {
				actionExit();
			}
		});

		// Set up file menu.
		// �ֻ���� File�� ������ EXIT�� �����µ� �̰͵� UI�� �����µ� �̿��� �� �ְ��ϴ� ����
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		JMenuItem fileExitMenuItem = new JMenuItem("Exit", KeyEvent.VK_X);
		fileExitMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionExit();
			}
		});
		fileMenu.add(fileExitMenuItem);
		menuBar.add(fileMenu);
		setJMenuBar(menuBar);
		// ������� FILE -> EXIT�� ���� ����

		// Set up search panel.
		JPanel searchPanel = new JPanel();
		// �г�(�ǳ�)�� ������ ���̴�. ���⿡ �츮�� ���� UI�� ������ ���δ�.

		GridBagConstraints constraints;
		// GridBagConstraints Ŭ������,GridBagLayout Ŭ������ ����� ��ġ�Ǵ� ���۳�Ʈ�� ������ �����մϴ�.
		GridBagLayout layout = new GridBagLayout();
		// GridBagLayout Ŭ������, �ٸ� ũ���� ���۳�Ʈ������ ��Ⱦ��,
		// �Ǵ� baseline�� ���� ��ġ�� �� �ִ� ������ ���̾ƿ� �Ŵ����Դϴ�.

		//
		searchPanel.setLayout(layout);// �гο� �츮�� ���� ���̾ƿ��� ���� �ڴٰ� ������

		// File�ٷ� �Ʒ��� ���� �ؿ�

		// Start URL : [] ���� ���� ����
		// ////////////////////////////////////////////////////////////
		// Start URL : [] ���� Label ���� ����
		JLabel startLabel = new JLabel("Start URL:");
		constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.EAST; // �Ʒ� ��ġ(inset)���� ������ ����
		constraints.insets = new Insets(5, 5, 0, 0); // Start URL : ���� ��� ��ġ��
		// �̰����� ���� �ȴ�.
		layout.setConstraints(startLabel, constraints);
		// ���� �гο� ���̵� �̸��� Start URL�� Inset�� ���� ��ġ�� ���̶��...
		searchPanel.add(startLabel);
		// Start URL : [] Label ���� ���� ��
		// Start URL : [] ���� TextField ���� ����
		startTextField = new JTextField();
		constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.insets = new Insets(5, 5, 0, 5);
		layout.setConstraints(startTextField, constraints);
		searchPanel.add(startTextField);
		// Start URL : [] ���� TextField ���� ��
		// Start URL : [] ���� ���� ��
		// /////////////////////////////////////////////////////////////

		// �Ʒ����ʹ� ���� ����, �� Start URL�� �����ϰ� �����ϱ� �ٶ�
		// Max URLs to Crawl:
		// ����///////////////////////////////////////////////////////////////
		// ��
		JLabel maxLabel = new JLabel("Max URLs to Crawl:");
		constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.EAST;
		constraints.insets = new Insets(5, 5, 0, 0);
		layout.setConstraints(maxLabel, constraints);
		searchPanel.add(maxLabel);
		// �޺��ڽ�
		maxComboBox = new JComboBox(MAX_URLS);
		maxComboBox.setEditable(true);
		constraints = new GridBagConstraints();
		constraints.insets = new Insets(5, 5, 0, 0);
		layout.setConstraints(maxComboBox, constraints);
		searchPanel.add(maxComboBox);
		// üũ�ڽ�
		limitCheckBox = new JCheckBox("Limit crawling to Start URL site");
		constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.WEST;
		constraints.insets = new Insets(0, 10, 0, 0);
		layout.setConstraints(limitCheckBox, constraints);
		searchPanel.add(limitCheckBox);
		// üũ�ڽ��� ������ ���� �����Ѵ�. �������� �������� �� ���ΰ� �˼� ����
		JLabel blankLabel = new JLabel();
		constraints = new GridBagConstraints();
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(blankLabel, constraints);
		searchPanel.add(blankLabel);
		// Max URLs to Crawl:
		// ��/////////////////////////////////////////////////////////////////

		// Matches Log File: ����
		// //////////////////////////////////////////////////////////////
		// ��
		JLabel logLabel = new JLabel("Matches Log File:");
		constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.EAST;
		constraints.insets = new Insets(5, 5, 0, 0);
		layout.setConstraints(logLabel, constraints);
		searchPanel.add(logLabel);
		// �ؽ�Ʈ ���Ͽ� �ڵ����� ��ΰ� ���� ���ε� �̰��� �غ�
		String file = System.getProperty("user.dir")
		+ System.getProperty("file.separator") + "crawler.log";
		logTextField = new JTextField(file);
		constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.insets = new Insets(5, 5, 0, 5);
		layout.setConstraints(logTextField, constraints);
		searchPanel.add(logTextField);
		// Matches Log File: ��
		// ///////////////////////////////////////////////////////////////

		// Search String: ����
		// /////////////////////////////////////////////////////////////////
		// ��
		JLabel searchLabel = new JLabel("Search String:");
		constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.EAST;
		constraints.insets = new Insets(5, 5, 0, 0);
		layout.setConstraints(searchLabel, constraints);
		searchPanel.add(searchLabel);
		// �ؽ�Ʈ �ڽ�
		searchTextField = new JTextField();
		constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(5, 5, 0, 0);
		constraints.gridwidth = 2;
		constraints.weightx = 1.0d;
		layout.setConstraints(searchTextField, constraints);
		searchPanel.add(searchTextField);
		// üũ�ڽ�
		caseCheckBox = new JCheckBox("Case Sensitive");
		constraints = new GridBagConstraints();
		constraints.insets = new Insets(5, 5, 0, 5);
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(caseCheckBox, constraints);
		searchPanel.add(caseCheckBox);
		// Search String: ��
		// //////////////////////////////////////////////////////////////////////

		// Search ��ư ����
		// ///////////////////////////////////////////////////////////////////////
		searchButton = new JButton("Search");
		searchButton.addActionListener(new ActionListener() {
			public
			void actionPerformed(ActionEvent e) {
				actionSearch();
			}
		});
		constraints = new GridBagConstraints();
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.insets = new Insets(5, 5, 5, 5);
		layout.setConstraints(searchButton, constraints);
		searchPanel.add(searchButton);
		// Search ��ư ��
		// ////////////////////////////////////////////////////////////////////////////////

		// ����
		// ����-----------------------------------------------------------------------------------------
		JSeparator separator = new JSeparator();
		constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.insets = new Insets(5, 5, 5, 5);
		layout.setConstraints(separator, constraints);
		searchPanel.add(separator);
		// ����
		// ��-----------------------------------------------------------------------------------------

		// Crawling: ����
		// ///////////////////////////////////////////////////////////////////////
		JLabel crawlingLabel1 = new JLabel("Crawling:");
		constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.EAST;
		constraints.insets = new Insets(5, 5, 0, 0);
		layout.setConstraints(crawlingLabel1, constraints);
		searchPanel.add(crawlingLabel1);
		// ȭ�鿡 �������� Label�̿� �ϳ��� �� �����Ѵ�. (������ ���� ��)
		crawlingLabel2 = new JLabel();
		crawlingLabel2.setFont(crawlingLabel2.getFont().deriveFont(Font.PLAIN));
		constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.insets = new Insets(5, 5, 0, 5);
		layout.setConstraints(crawlingLabel2, constraints);
		searchPanel.add(crawlingLabel2);
		// Crawling: ��
		// ////////////////////////////////////////////////////////////////////////

		// Crawled URLs: ����
		// /////////////////////////////////////////////////////////////////
		JLabel crawledLabel1 = new JLabel("Crawled URLs:");
		constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.EAST;
		constraints.insets = new Insets(5, 5, 0, 0);
		layout.setConstraints(crawledLabel1, constraints);
		searchPanel.add(crawledLabel1);
		// ����� Label�� �ϴ� �� �ִ�. (���� ���� ����~~~~~~~)
		crawledLabel2 = new JLabel();
		crawledLabel2.setFont(crawledLabel2.getFont().deriveFont(Font.PLAIN));
		constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.insets = new Insets(5, 5, 0, 5);
		layout.setConstraints(crawledLabel2, constraints);
		searchPanel.add(crawledLabel2);
		// Crawled URLs: ��
		// ////////////////////////////////////////////////////////////////////

		// URLs to Crawl: ����
		// ////////////////////////////////////////////////////////////////
		JLabel toCrawlLabel1 = new JLabel("URLs to Crawl:");
		constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.EAST;
		constraints.insets = new Insets(5, 5, 0, 0);
		layout.setConstraints(toCrawlLabel1, constraints);
		searchPanel.add(toCrawlLabel1);
		// ���⵵ Label�� �� �ִ� ������ ���� �κ��� �ΰ� �ִ��� �ƴϸ� �ι� ���� �� �� ����.
		toCrawlLabel2 = new JLabel();
		toCrawlLabel2.setFont(toCrawlLabel2.getFont().deriveFont(Font.PLAIN));
		constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.insets = new Insets(5, 5, 0, 5);
		layout.setConstraints(toCrawlLabel2, constraints);
		searchPanel.add(toCrawlLabel2);
		// URLs to Crawl: ��
		// ////////////////////////////////////////////////////////////////////////

		// Crawling Progress: ����
		// //////////////////////////////////////////////////////////////
		JLabel progressLabel = new JLabel("Crawling Progress:");
		constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.EAST;
		constraints.insets = new Insets(5, 5, 0, 0);
		layout.setConstraints(progressLabel, constraints);
		searchPanel.add(progressLabel);
		// ���α׷��� ��
		progressBar = new JProgressBar();
		progressBar.setMinimum(0);
		progressBar.setStringPainted(true);
		constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.insets = new Insets(5, 5, 0, 5);
		layout.setConstraints(progressBar, constraints);
		searchPanel.add(progressBar);
		// Crawling Progress: ��
		// /////////////////////////////////////////////////////////////////

		// Search Matches: ����
		// ////////////////////////////////////////////////////////////////
		JLabel matchesLabel1 = new JLabel("Search Matches:");
		constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.EAST;
		constraints.insets = new Insets(5, 5, 10, 0);
		layout.setConstraints(matchesLabel1, constraints);
		searchPanel.add(matchesLabel1);

		matchesLabel2 = new JLabel();
		matchesLabel2.setFont(matchesLabel2.getFont().deriveFont(Font.PLAIN));
		constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.insets = new Insets(5, 5, 10, 5);
		layout.setConstraints(matchesLabel2, constraints);
		searchPanel.add(matchesLabel2);
		// Search Matches: ��
		// ///////////////////////////////////////////////////////////////////

		// Set up matches table.
		// URL�̶�� ������ ����� �޾� �ִ� �� : ���̺�� ������� 1�� URL 2�� ��� �ǽðڴ�~
		table = new JTable(new DefaultTableModel(new Object[][] {},
				new String[] { "URL" }) {
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		});

		// Set up matches panel.
		// Matches��� ���� �ִ� �� �ֺ��� ���� ���� ������ ���δ�. �Ǵٸ� �г��� �̰��� ���̽� �������� �ڱ��ǽðڴ�.
		// �޴��� ���� ��ȣ�ϴ� �ʸ� �ΰ� �پ����� �װ͵� ���� Ʋ������... �̷��� ���̴µ� �Ȱ��ٰ� �Ǵܵȴ�.
		JPanel matchesPanel = new JPanel();
		matchesPanel.setBorder(BorderFactory.createTitledBorder("Matches"));
		matchesPanel.setLayout(new BorderLayout());
		matchesPanel.add(new JScrollPane(table), BorderLayout.CENTER);

		// Add panels to display.
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(searchPanel, BorderLayout.NORTH);
		getContentPane().add(matchesPanel, BorderLayout.CENTER);
	} // ///////////////////////////////////////////////////////////////////////////UI
	// ��....

	// Exit this program. //���α׷� ���� ������ ���� �� ȣ��
	private void actionExit() {
		System.exit(0);
	}

	// Handle search/stop button being clicked. Search �Ǵ� Stop ��ư�� Ŭ���Ǹ� ���Ⱑ ȣ��
	// �ȴ�.
	private void actionSearch() {
		// If stop button clicked, turn crawling flag off.
		if (crawling) {
			crawling = false;
			return;
		}

		ArrayList<String> errorList = new ArrayList<String>(); // ������ ��� ����Ʈ

		// Validate that start URL has been entered. => startURL�� ����ڰ� ���� ������
		// ��´�.
		String startUrl = startTextField.getText().trim();
		// startURL�� ��ȿ�� �˻�
		if (startUrl.length() < 1) {
			errorList.add("Missing Start URL.");
		}
		// Verify start URL.
		else if (verifyUrl(startUrl) == null) {
			errorList.add("Invalid Start URL.");
		}

		// Validate that max URLs is either empty or is a number.
		// => ����ڰ� Max URLs to Crawl�� ���� ������ ��´�.
		int maxUrls = 0;
		String max = ((String) maxComboBox.getSelectedItem()).trim();
		if (max.length() > 0) {
			try {
				maxUrls = Integer.parseInt(max);
			} catch (NumberFormatException e) {
			}
			if (maxUrls < 1) {
				errorList.add("Invalid Max URLs value.");
			}
		}

		// Validate that matches log file has been entered.
		// Matches Log File�� ���� ������ ��´�.
		String logFile = logTextField.getText().trim();
		if (logFile.length() < 1) {
			errorList.add("Missing Matches Log File.");
		}

		// Validate that search string has been entered.
		// ����ڰ� Search String�� ���� ������ ��´�.
		String searchString = searchTextField.getText().trim();
		if (searchString.length() < 1) {
			errorList.add("Missing Search String.");
		}

		// Show errors, if any, and return.//�����޼��� ��� 
		if (errorList.size() > 0) {
			StringBuffer message = new StringBuffer();//�����޼����� ���� ���� ����

			// Concatenate errors into single message. //���� ����Ʈ�� �ϳ��� �޼����� �����
			// ���ۿ� ����
			for (int i = 0; i < errorList.size(); i++) {
				message.append(errorList.get(i));
				if (i + 1 < errorList.size()) {
					message.append("\n");
				}
			}

			showError(message.toString());
			return;
		}

		// Remove "www" from start URL if present. 
		startUrl = removeWwwFromUrl(startUrl); //removeWwwFromUrl() ȣ�� 

		// Start the search crawler.
		search(logFile, startUrl, maxUrls, searchString); //search() ȣ��
	}

	private void search(final String logFile, final String startUrl,
			final int maxUrls, final String searchString) {
		// Start the search in a new thread.
		Thread thread = new Thread(new Runnable() { //thread ����
			public void run() { //thread ���� 
				// Show hour glass cursor while crawling is under way.
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));//�𷡽ð� Ŀ���� �ٲ�

				// Disable search controls. //�κ�Ȱ���߿� �Է��� ���� ��Ŵ 
				startTextField.setEnabled(false);
				maxComboBox.setEnabled(false);
				limitCheckBox.setEnabled(false);
				logTextField.setEnabled(false);
				searchTextField.setEnabled(false);
				caseCheckBox.setEnabled(false);

				// Switch search button to "Stop." //�κ� Ȱ�� �߿� search ��ư�� stop��ư���� �ٲ���
				searchButton.setText("Stop");

				// Reset stats.
				table.setModel(new DefaultTableModel(new Object[][] {},//URL�Ʒ� Table�� ���� ����
						new String[] { "URL" }) {
					public boolean isCellEditable(int row, int column) {
						//�ѿ��� ���� ������� ���̺� ���� ��Ÿ���� ���� ���� -> ���� �̻��ϸ� false
						return false;
					}
				});
				updateStats(startUrl, 0, 0, maxUrls); //updateStatesȣ�� 

				// Open matches log file. //�α������� ���� ���� �غ� 
				try {
					logFileWriter = new PrintWriter(new FileWriter(logFile));
				} catch (Exception e) {
					showError("Unable to open matches log file.");
					return;
				}

				// Turn crawling flag on. //flag�� �κ��� Ȱ���� �˸� ǥ�� 
				crawling = true;

				// Perform the actual crawling.//crawl() ȣ�� 
				crawl(startUrl, maxUrls, limitCheckBox.isSelected(), //is selected�� üũ ���¸� Ȯ���ϴ� ��
						searchString, caseCheckBox.isSelected());

				// Turn crawling flag off. //flag�� �κ��� Ȱ���� �����Ǿ����� �˸���. 
				crawling = false;

				// Close matches log file. //�α������� �������� writer�� �ݴ´�. 
				try {
					logFileWriter.close();
				} catch (Exception e) {
					showError("Unable to close matches log file.");
				}

				// Mark search as done. // ������ �󺧿� Ȱ���� �������� 'done'���� �˸� ��
				crawlingLabel2.setText("Done");

				// Enable search controls. //����ڰ� �ؽ�Ʈ �ʵ带 ����� �� �ֵ��� Ȱ��ȭ �� 
				startTextField.setEnabled(true);
				maxComboBox.setEnabled(true);
				limitCheckBox.setEnabled(true);
				logTextField.setEnabled(true);
				searchTextField.setEnabled(true);
				caseCheckBox.setEnabled(true);

				// Switch search button back to "Search." //��ư�� stop -> search�� �ٲ� 
				searchButton.setText("Search");

				// Return to default cursor.
				setCursor(Cursor.getDefaultCursor());//Ŀ���� �𷡽ð迡�� �Ϲ� Ŀ���� �ٲ� 

				// Show message if search string not found.
				if (table.getRowCount() == 0) { //�˻��� ���� ���� ����� ��� 
					JOptionPane
					.showMessageDialog(
							SearchCrawler.this,
							"Your Search String was not found. Please try another.",
							"Search String Not Found",
							JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		thread.start();
	}

	// Show dialog box with error message.
	private void showError(String message) {
		JOptionPane.showMessageDialog(this, message, "Error",
				JOptionPane.ERROR_MESSAGE);
	}

	// Update crawling stats.
	private void updateStats(String crawling, int crawled, int toCrawl,
			int maxUrls) { //������ UI �����Ҷ� ���� �� 2�����ΰ��� ���� ����� ���⼭ Ǯ����. 
						   // ������ ���¿��� ǥ�õǴ� ���� �ϳ� �� �ִ�. 
		//=> �ؽ�Ʈ�ʵ�� ����� ����ڰ� �Է��ϱ⶧���� �̰��� �����ϱ� ���� �󺧷� ���� 
		crawlingLabel2.setText(crawling);
		crawledLabel2.setText("" + crawled);
		toCrawlLabel2.setText("" + toCrawl);

		// Update progress bar. //���α׷��� ���� �ִ밪 ���� 
		if (maxUrls == -1) { //�ִ� ������ �۴ٸ� ������ �� + ������ �� 
			// �̸� ������ ������ ����� �ľ��� ������ �� �ϴ�. 
			progressBar.setMaximum(crawled + toCrawl);
		} else { //���� ��찡 �ƴҰ�� ����� ���� �ִ���� ��
			progressBar.setMaximum(maxUrls); 
		}
		progressBar.setValue(crawled); //������ ������ ���α׷����ٸ� ���� 

		matchesLabel2.setText("" + table.getRowCount());// ���� ���� ��ų �� ���� �ߴ´�. 
	}

	// Add match to matches table and log file.
	private void addMatch(String url) { //���̺� log�� ����Ѵ�. 
		// Add URL to matches table.
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		model.addRow(new Object[] { url });

		// Add URL to matches log file.
		try {
			logFileWriter.println(url);
		} catch (Exception e) {
			showError("Unable to log match.");
		}
	}

	// Verify URL format. //url������ �˻� 
	private URL verifyUrl(String url) {
		// Only allow HTTP URLs.
		if (!url.toLowerCase().startsWith("http://"))
			return null;

		// Verify format of URL.
		URL verifiedUrl = null;
		try {
			verifiedUrl = new URL(url);
		} catch (Exception e) {
			return null;
		}

		return verifiedUrl;
	}

	// Check if robot is allowed to access the given URL.
	@SuppressWarnings("unchecked") //�κ� ���������� �����ϱ� ���� �޼��� 
	private boolean isRobotAllowed(URL urlToCheck) {
		String host = urlToCheck.getHost().toLowerCase();

		// Retrieve host's disallow list from cache. //�κ� �������ݿ� ���� �ʴ� ���� ���� ����Ʈ 
		ArrayList<String> disallowList = (ArrayList<String>) disallowListCache.get(host);

		// If list is not in the cache, download and cache it.
		if (disallowList == null) {
			disallowList = new ArrayList();

			try {
				URL robotsFileUrl = new URL("http://" + host + "/robots.txt");

				// Open connection to robot file URL for reading.
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(robotsFileUrl.openStream()));

				// Read robot file, creating list of disallowed paths.
				String line;
				while ((line = reader.readLine()) != null) {
					if (line.indexOf("Disallow:") == 0) {
						String disallowPath = line.substring("Disallow:"
								.length());

						// Check disallow path for comments and remove if
						// present.
						int commentIndex = disallowPath.indexOf("#");
						if (commentIndex != -1) {
							disallowPath = disallowPath.substring(0,
									commentIndex);
						}

						// Remove leading or trailing spaces from disallow path.
						disallowPath = disallowPath.trim();

						// Add disallow path to list.
						disallowList.add(disallowPath);
					}
				}

				// Add new disallow list to cache.
				disallowListCache.put(host, disallowList);
			} catch (Exception e) {
				/*
				 * Assume robot is allowed since an exception is thrown if the
				 * robot file doesn't exist.
				 */
				return true;
			}
		}

		/*
		 * Loop through disallow list to see if the crawling is allowed for the
		 * given URL.
		 */
		String file = urlToCheck.getFile();
		for (int i = 0; i < disallowList.size(); i++) {
			String disallow = (String) disallowList.get(i);
			if (file.startsWith(disallow)) {
				return false;
			}
		}

		return true;
	}

	// Download page at given URL. //�˻縦 ����� url�� �ٿ�ε� �Ѵ�. 
	private String downloadPage(URL pageUrl) {
		try {
			// Open connection to URL for reading.
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					pageUrl.openStream()));

			// Read page into buffer.
			String line;
			StringBuffer pageBuffer = new StringBuffer();
			while ((line = reader.readLine()) != null) {
				pageBuffer.append(line);
			}

			return pageBuffer.toString();
		} catch (Exception e) {
		}

		return null;
	}

	// Remove leading "www" from a URL's host if present.
	// removeWwwFromUrl()�� ȣ���Ͽ� �Է��ѡ�url���� www���� ����
	private String removeWwwFromUrl(String url) {
		int index = url.indexOf("://www.");
		if (index != -1) {
			return url.substring(0, index + 3) + url.substring(index + 7);
		} //(��) http://www.daum.net=> http://daum.net
		return (url);
	}

	// Parse through page contents and retrieve links.
	private ArrayList<String> retrieveLinks(URL pageUrl, String pageContents,
			HashSet crawledList, boolean limitHost) {
		// Compile link matching pattern.
		Pattern p = Pattern.compile("<a\\s+href\\s*=\\s*\"?(.*?)[\"|>]", //<a href=""> �̷� ���� �ɷ���
				Pattern.CASE_INSENSITIVE); //String�� �����ϵɶ� ������ ���� -> JAVA�� ����ǥ���� 
		Matcher m = p.matcher(pageContents); //����ǥ���Ŀ� �´��� ��Ī���Ѻ���. 

		// Create list of link matches.
		ArrayList<String> linkList = new ArrayList<String>();
		while (m.find()) { //��ġ�� �Ǹ� ��� ���ư���. 
			String link = m.group(1).trim(); // s.substring(m.start(1), m.end(1))�� ���� ǥ�� 
											 // ��Ī�� ��  �� ù��°���� link��  �߶󳻾� ���� 
			// Skip empty links. //��ũ�� ����ִٸ� 
			if (link.length() < 1) {
				continue;
			}

			// Skip links that are just page anchors.
			if (link.charAt(0) == '#') { //#�� �� ��ũ��� 
				continue;
			}

			// Skip mailto links.
			if (link.indexOf("mailto:") != -1) {//������ link�Ǿ� �ִٸ� 
				continue;
			}

			// Skip JavaScript links. //javascript�� link �Ǿ� �ִٸ� 
			if (link.toLowerCase().indexOf("javascript") != -1) {
				continue;
			}

			// Prefix absolute and relative URLs if necessary.
			if (link.indexOf("://") == -1) { // �̷� ����(://)�� ���ٸ� 
				// Handle absolute URLs.
				if (link.charAt(0) == '/') { // ������(/)�� �����ϴ� ���� url�� ��� (��) /blog/hagi
					link = "http://" + pageUrl.getHost() + link; //�տ� http://�� ���δ�. 
					// Handle relative URLs.
				} else { //��� ��� 
					String file = pageUrl.getFile();
					if (file.indexOf('/') == -1) { /* index.html�� ���� ��� (�������� ���� ���� ���)*/ 
						link = "http://" + pageUrl.getHost() + "/" + link;
					} else { /* blog/hagi ���� ������(/)�� ���� ���� ���  */
						String path = file.substring(0,
								file.lastIndexOf('/') + 1);
						link = "http://" + pageUrl.getHost() + path + link;
					}
				}
			}

			// Remove anchors from link. //link�� #�� �ִٸ� �������� �ڸ���. 
			int index = link.indexOf('#');
			if (index != -1) {
				link = link.substring(0, index);
			}

			// Remove leading "www" from URL's host if present.
			link = removeWwwFromUrl(link); //www�� �߶� 

			// Verify link and skip if invalid.
			URL verifiedLink = verifyUrl(link);
			if (verifiedLink == null) {
				continue;
			}

			/*
			 * If specified, limit links to those having the same host as the
			 * start URL.
			 *    ���� �κ� ���� �������� http://daum.net�̰�  
			 */ //abc.html�� �����ּҰ� http://daum.net�̶�� �̰��� �ɷ�����. 
			if (limitHost
					&& !pageUrl.getHost().toLowerCase().equals(
							verifiedLink.getHost().toLowerCase())) {
				continue;
			}

			// Skip link if it has already been crawled.
			if (crawledList.contains(link)) { //�̹� crawledList���� ������ ������� �Ѿ��. 
				continue;
			}

			// Add link to list.
			linkList.add(link); //��� �ƴ϶�� linkList�� ���� 
		}

		return (linkList);
	}

	/*
	 * Determine whether or not search string is matched in the given page
	 * contents.
	 */
	private boolean searchStringMatches(String pageContents,
			String searchString, boolean caseSensitive) {
		String searchContents = pageContents;

		/*
		 * If case sensitive search, lowercase page contents for comparison.
		 */
		if (!caseSensitive) { //��ҹ��ڸ� ���� ���Ҷ� 
			searchContents = pageContents.toLowerCase();
		}

		// Split search string into individual terms.
		Pattern p = Pattern.compile("[\\s]+"); //�����϶��� �������� ������ �ش�. 
		String[] terms = p.split(searchString); //������ �������� �ڸ���. 

		// Check to see if each term matches.
		for (int i = 0; i < terms.length; i++) { //������ ���뿡 ã�� �ܾ �ִ��� �Ǵ��Ͽ� ���� 
			if (caseSensitive) {
				if (searchContents.indexOf(terms[i]) == -1) {
					return false;
				}
			} else {
				if (searchContents.indexOf(terms[i].toLowerCase()) == -1) {
					return false;
				}
			}
		}

		return true;
	}

	// Perform the actual crawling, searching for the search string.
	public void crawl(String startUrl, int maxUrls, boolean limitHost, //�κ��� ���������� Ȱ�� 
			String searchString, boolean caseSensitive) {
		// Setup crawl lists. -> ���� ������ ����ϱ� ���� HashSet ���� 
		HashSet crawledList = new HashSet(); //������ ���� ���� ����ϱ� ���� �� 
		LinkedHashSet toCrawlList = new LinkedHashSet(); //������ ������ ���� ����ϱ� ���� �� 

		// Add start URL to the to crawl list.
		toCrawlList.add(startUrl); //�����ϱ� ���� ����Ʈ�� ����ڰ� ��������� ������ ����Ʈ�� ���

		/*
		 * Perform actual crawling by looping through the to crawl list.
		 */
		while (crawling && toCrawlList.size() > 0) { 
			// ������ �����ٴ� ��ȣ�� �ְų� ������ ������ ����Ʈ�� 0�̻��� ������ �ݺ�~
			/*
			 * Check to see if the max URL count has been reached, if it was
			 * specified.
			 */
			if (maxUrls != -1) { //�ִ� url������ ���� �Ǿ� �ִٸ� 
				if (crawledList.size() == maxUrls) { //������ ����Ʈ�� ������ �ִ� url���� ������ ����!!
					break;
				}
			}

			// Get URL at bottom of the list. //������ ������ ����Ʈ�� ���� ���� url�� ����
			String url = (String) toCrawlList.iterator().next();

			// Remove URL from the to crawl list.//������ ������ ����Ʈ�� ���� �����.
			toCrawlList.remove(url);             //����� ���� ���� 1���̸� ���⼭ ����� 0�� �� ����

			// Convert string url to URL object. //������ �Է¹��� url�� ���� �˻縦 �ϴ� �� ����
			URL verifiedUrl = verifyUrl(url);  //URL�̶�� Ŭ������ java�� �⺻ ���� �Ǿ� ���� 
											//RFC 2396: Uniform Resource Identifiers (URI): Generic Syntax, 
											//��� ������ �´��� �˻� 
			// Skip URL if robots are not allowed to access it.
			if (!isRobotAllowed(verifiedUrl)) {//������ ���� ������ ������ ���� �ʰ� �������� �Ѿ� ����. 
				continue;
			}

			// Update crawling stats. //������ �´ٸ� �κ� ���¸� �����Ѵ�. 
			updateStats(url, crawledList.size(), toCrawlList.size(), maxUrls);

			// Add page to the crawled list. //������ ����Ʈ�� url�� �ִ´�. 
			crawledList.add(url);

			// Download the page at the given url. //downloadpage�� ȣ��,�����Ͽ� ���ϰ��� pageContent�� ����
			String pageContents = downloadPage(verifiedUrl); //�������� ���� �о� �鿩 ����

			/*
			 * If the page was downloaded successfully, retrieve all of its
			 * links and then see if it contains the search string.
			 */
			if (pageContents != null && pageContents.length() > 0) { //�������� ������ ���� ��� 
				// Retrieve list of valid links from page.
				ArrayList<String> links = retrieveLinks(verifiedUrl, pageContents,
						crawledList, limitHost); //limitHost??? �ƹ����� ���� �Ǿ� ���� �ʴµ� �� �𸣰ڴ�

				// Add links to the to crawl list.
				toCrawlList.addAll(links); //������ ��� link�� toCrawlList�� ��´�. 

				/*
				 * Check if search string is present in page and if so record a
				 * match.
				 */
				if (searchStringMatches(pageContents, searchString,
						caseSensitive)) { 
					addMatch(url); //������ ���뿡 ã�� �ܾ� �� �ִٸ� url�� ���̺��� ����Ѵ�. 
				}
			}

			// Update crawling stats. //������ �󺧰� ���α׷����ٸ� ������Ʈ�Ѵ�. 
			updateStats(url, crawledList.size(), toCrawlList.size(), maxUrls);
		}
	}

	// Run the Search Crawler.
	public static void main(String[] args) {
		SearchCrawler crawler = new SearchCrawler();
		crawler.show();
	}
}
