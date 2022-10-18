package application;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.DirectoryChooser;

public class SampleController {

	@FXML
	private TextField name, bilder, speicher;

	@FXML
	private Label progLabel;
	
	@FXML
	private ToggleGroup tgA;

	@FXML
	private RadioButton rbL, rbM, rbR, rbV;

	@FXML
	private ProgressBar pgBar;
	
	@FXML
	private ProgressIndicator progIndi;

	private File imageSrc, targetSrc;

	@FXML
	public void getImageSrc(ActionEvent event) {
		int checki = 0;
		DirectoryChooser chooser = new DirectoryChooser();
		this.imageSrc = chooser.showDialog(null);
		if (this.imageSrc != null) {
			File[] check = this.imageSrc.listFiles();
			for(int i=0;i<check.length;i++) {
				if(!check[i].getName().endsWith(".png")) {
					checki++;
					if(checki>2) {
						bilder.setText(null);
						Alert text = new Alert(AlertType.ERROR);
						text.setHeaderText("Im BilderOrdner d¸rfen nur .png Datein sein");
						text.show();
						return;
					}
				}
			}
			bilder.setText(this.imageSrc.getPath());
			
		} else if (this.imageSrc == null) {
			bilder.setText(null);
		}

	}

	@FXML
	public void getTargetSrc(ActionEvent event) {
		DirectoryChooser chooser = new DirectoryChooser();
		this.targetSrc = chooser.showDialog(null);
		if (this.targetSrc != null) {
			speicher.setText(this.targetSrc.getPath());
		} else if (this.targetSrc == null) {
			speicher.setText(null);
		}
	}

	private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
		if (fileToZip.isHidden()) {
			return;
		}
		if (fileToZip.isDirectory()) {
			File[] children = fileToZip.listFiles();
			for (File childFile : children) {
				zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
			}
			return;
		}
		FileInputStream fis = new FileInputStream(fileToZip);
		ZipEntry zipEntry = new ZipEntry(fileName);
		zipOut.putNextEntry(zipEntry);
		byte[] bytes = new byte[1024];
		int length;
		while ((length = fis.read(bytes)) >= 0) {
			zipOut.write(bytes, 0, length);
		}
		fis.close();
	}

	private void zipping(File sourceFile) {
		try {
			FileOutputStream fos = new FileOutputStream(sourceFile + ".h5p");
			ZipOutputStream zipOut = new ZipOutputStream(fos);
			File fileToZip = new File(sourceFile.getPath());

			File[] children1 = fileToZip.listFiles();
			for (File childFile : children1) {
				zipFile(childFile, childFile.getName(), zipOut);
			}
			zipOut.flush();
			fos.flush();
			zipOut.close();
			fos.close();

		} catch (Exception e) {
			System.out.println("didnt work");
		}
	}

	@FXML
	public void createH5p(ActionEvent event) {

		Alert err = new Alert(AlertType.ERROR);
		File blank = new File("blanko");
		// Abcheken ob alle Felder ausgew‰hlt worden sind!
		
		if (name.getText().isEmpty()) {
			err.setTitle("Es Wurde kein Name gefunden");
			err.setHeaderText("Bitte vorher einen Namen ausw√§hlen");
			err.show();
		}else if(!blank.exists()) {
			err.setTitle("Blanko Ordner Fehlt");
			err.setHeaderText("der Blanko Ordner muss im gleichen Verzeichniss wie die Mica.jar liegen!");
			err.show();
		}
		else if (imageSrc == null) {
			err.setTitle("Es Wurde Kein Bilder Ordner gefunden");
			err.setHeaderText("Bitte vorher einen Bilder Ordner w√§hlen");
			err.show();
		} else if (targetSrc == null) {
			err.setTitle("Es Wurde Kein Speicher Ort gefunden");
			err.setHeaderText("Bitte vorher einen Speicher Ort ausw√§hlen");
			err.show();
		} else {

			Task<Void> task = new Task<Void>() {
				@Override
				protected Void call() throws Exception {

					try {

					
						String bslh = File.separator;
						File blanko = new File("blanko");
						
						
						File targetblanko = new File(targetSrc.getPath() + bslh + name.getText());
						updateProgress(15, 100);
						
						FileUtils.copyDirectory(blanko, targetblanko);

						// Bilder aus angegeben Ordner ins h5p verzeichnis Kopieren
						File[] bilderSrc = imageSrc.listFiles();
						File OrdnerInBilder = new File(
								targetSrc.getPath() + bslh + name.getText() + bslh + "content" + bslh + "images");
						FileUtils.copyDirectory(imageSrc, OrdnerInBilder);
						updateProgress(35, 100);
						// Json Datei einlesen
						BufferedReader in = new BufferedReader(
								new FileReader("blanko" + bslh + "content" + bslh + "content.json"));
						BufferedWriter out = new BufferedWriter(new FileWriter(targetSrc.getPath() + bslh
								+ name.getText() + bslh + "content" + bslh + "content.json"));

						String zeile = null;

						// Gew¸nschte Ausrichtung abchecken
						double x = 0;
						double bret = 67.5;
						if (rbR.isSelected()) {
							x = 32.6;
						} else if (rbM.isSelected()) {
							x = 16.0;
						}else if (rbV.isSelected()) {
							bret = 100;
						}

						// Json Datei manipulieren
						while ((zeile = in.readLine()) != null) {

							if (zeile.contains("\"slides\"")) {
								
								out.write(zeile);
								updateProgress(50, 100);
								
								LinkedList<Integer> breite = new LinkedList<Integer>();
								LinkedList<Integer> hohe = new LinkedList<Integer>();
							
	
								for(int p=0; p<bilderSrc.length;p++) {
									if(bilderSrc[p].getName().endsWith(".png")) {
										BufferedImage image = ImageIO.read(bilderSrc[p]);
										breite.add(image.getWidth());
										hohe.add(image.getHeight());
										
									}
								}
								
								
								
								
								for (int i = 1; i < breite.size(); i++) {
									String bildpart = "{ \"elements\": [ { \"x\": " + x
											+ ", \"y\": 0, \"width\": "+bret+", \"height\": 100,";
									bildpart += "\"action\": { \"library\": \"H5P.Image 1.1\", \"params\": { \"contentName\": \"Bild\", \"file\":";
									bildpart += "{ \"path\": \"images\\/Folie" + i + ".png"
											+ "\", \"mime\": \"image\\/png\", \"copyright\": { \"license\": \"U\" },";
									bildpart += "\"width\": "+breite.get(i-1)+",\"height\": "+hohe.get(i-1)+"} },";
									bildpart += "\"metadata\": { \"contentType\": \"Image\", \"license\": \"U\", \"title\": \"Folie"
											+ i
											+ "\" } }, \"alwaysDisplayComments\": false, \"backgroundOpacity\": 0, \"displayAsButton\": false, \"buttonSize\": \"big\", \"goToSlideType\": \"specified\", \"invisible\": false, \"solution\": \"\" } ], \"slideBackgroundSelector\": {} },";
									out.write(bildpart + "\n");
								}
								updateProgress(65, 100);

								String bildpart = "{ \"elements\": [ { \"x\": " + x
										+ ", \"y\": 0, \"width\": "+bret+", \"height\": 100,";
								bildpart += "\"action\": { \"library\": \"H5P.Image 1.1\", \"params\": { \"contentName\": \"Bild\", \"file\":";
								bildpart += "{ \"path\": \"images\\/Folie" + breite.size() + ".png"
										+ "\", \"mime\": \"image\\/png\", \"copyright\": { \"license\": \"U\" },";
								bildpart += "\"width\": "+breite.get(breite.size()-1)+",\"height\": "+hohe.get(hohe.size()-1)+"} },";
								bildpart += "\"metadata\": { \"contentType\": \"Image\", \"license\": \"U\", \"title\": \"Folie"
										+ (bilderSrc.length - 1)
										+ "\" } }, \"alwaysDisplayComments\": false, \"backgroundOpacity\": 0, \"displayAsButton\": false, \"buttonSize\": \"big\", \"goToSlideType\": \"specified\", \"invisible\": false, \"solution\": \"\" } ], \"slideBackgroundSelector\": {} }";
								out.write(bildpart + "\n");

								zeile = in.readLine();
							}
							out.write(zeile + "\n");
						}
						

						in.close();
						out.flush();
						out.close();
						updateProgress(75, 100);
						// Zum Schluss noch durch Zippen die H5P datei erstellen und den Ordner lˆschen.
						zipping(targetblanko);
						updateProgress(97, 100);
						FileUtils.deleteDirectory(targetblanko);

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					updateProgress(0, 100);
					

					return null;
				}
			};

			task.setOnFailed(wse -> {
				wse.getSource().getException().printStackTrace();
			});

			task.setOnSucceeded(wse -> {
				Alert errs = new Alert(AlertType.CONFIRMATION);
				errs.setAlertType(AlertType.CONFIRMATION);
				errs.setTitle("Erfolgreich !");
				errs.setHeaderText("Die H5P Datei wurde Erfolgreich erstellt");
				errs.show();
				progLabel.setText("");

			});
			
			task.setOnRunning(wse ->{
				progLabel.setText("Datei wird erstellt...");
			
			});
			

			pgBar.progressProperty().bind(task.progressProperty());
			progIndi.progressProperty().bind(task.progressProperty());
			

			new Thread(task).start();

		}

	}

	@FXML
	public void beenden(ActionEvent event) {
		System.exit(0);
	}

}
