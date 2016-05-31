package tftp_client_model;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Yannick Montes
 */
public class TFTPClient {
    
    // TFTP SERVER INFO
    private static final String TFTP_SERVER_IP = "192.168.43.202";// "localhost";
    private static final int TFTP_DEFAULT_PORT = 69;

    // TFTP OP Code
    private static final byte OP_RRQ = 1;
    private static final byte OP_WRQ = 2;
    private static final byte OP_DATAPACKET = 3;
    private static final byte OP_ACK = 4;
    private static final byte OP_ERROR = 5;

    private final static int PACKET_SIZE = 516;

    private DatagramSocket datagramSocket = null;
    private DatagramPacket datagramPacketEmission;
    private DatagramPacket datagramPacketReception;
    private byte[] sendMessage;
    private byte[] receivedMessage;
    
    /**
     * Base constructor
     */
    public TFTPClient()
    {
        //this.SendFile("Envoi.txt");
        //this.ReceiveFile("gobert.mp4");
    }
    
    /**
     * Cette fonction permet de réaliser le traitement de réception d'un fichier.
     * @param nameFile Le nom de fichier à demander au serveur.
     */
    public void ReceiveFile(String nameFile)
    {
        try {
            System.out.println("Demande au serveur le fichier "+nameFile);
            //Première étape, ouvrir le fichier en écriture locale.
            FileOutputStream file = new FileOutputStream(nameFile);
            
            //Deuxième étape, communication avec le serveur pour demande de lecture
            this.datagramSocket = new DatagramSocket();
            //On créé un message RRQ
            this.CreateRequestMessage(OP_RRQ, "octet", nameFile);
            //On créé un datagramePacket de réception
            this.datagramPacketEmission = new DatagramPacket(this.sendMessage, this.sendMessage.length, InetAddress.getByName(TFTP_SERVER_IP), TFTP_DEFAULT_PORT); 
            //On envoie notre requête
            this.datagramSocket.send(this.datagramPacketEmission);
            int cpt = 1;
            //Ensuite, on se met en reception jusqu'a la fin du fichier
            do
            {
                //On réinitialise les variables stockant le packet reçu
                this.receivedMessage = new byte[PACKET_SIZE];
                this.datagramPacketReception = new DatagramPacket(this.receivedMessage, PACKET_SIZE);
                //On se met en attente de réception
                this.datagramSocket.receive(this.datagramPacketReception);
                //System.out.println("Paquet numero "+(cpt++)+" reçu.");
                //On analyse le packet récupéré
                //Dans un premier temps on découpe pour récupérer les codes d'erreur et numéro de paquets
                byte[] opCode = {receivedMessage[0], receivedMessage[1]};
                if(opCode[0] == OP_ERROR)//Si il y a une erreur
                {
                    //Signaler une erreur
                }
                else if(this.datagramPacketReception.getData()[1] == OP_DATAPACKET)//Pas d'erreur, tout s'est bien passé.
                {
                    //On récupère le numéro de block pour envoyer le ACK identique.
                    byte[] blockNumber = {receivedMessage[2], receivedMessage[3]};
                    //On créé le message ACK
                    this.CreateAckMessage(blockNumber);
                    //On créé le packet que l'on va envoyer
                    this.datagramPacketEmission = new DatagramPacket(this.sendMessage, this.sendMessage.length, 
                            InetAddress.getByName(TFTP_SERVER_IP), this.datagramPacketReception.getPort());
                    //On envoie
                    this.datagramSocket.send(this.datagramPacketEmission);
                    //On traite ce qu'on a reçu (écriture dans le fichier)
                    //On commence a écrire a partir de la 4eme case (le reste étant les code et le numero de block)
                    //On arrête d'écrire a la longueur du packet reçu -4.
                    file.write(this.receivedMessage, 4, this.datagramPacketReception.getLength()-4);
                }                
            }while(!isLastPacket());
            
            System.out.println("Fichier correctement transféré. Transmission terminée, fermeture fichier local.");
            
            file.close();
            this.datagramSocket.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TFTPClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SocketException ex) {
            Logger.getLogger(TFTPClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownHostException ex) {
            Logger.getLogger(TFTPClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TFTPClient.class.getName()).log(Level.SEVERE, null, ex);
        }      
    }
    
    /**
     * Méthode permettant d'envoyer un fichier au serveur.
     * @param nameFile Le nom du fichier à envoyer au serveur.
     */
    public void SendFile(String nameFile)
    {
        try
        {
            //Premiere étape, communication avec le serveur pour demande d'écriture
            this.datagramSocket = new DatagramSocket();
            //On créé un message WRQ
            this.CreateRequestMessage(OP_WRQ, "octet", nameFile);
            //On créé un datagramePacket de réception
            this.datagramPacketEmission = new DatagramPacket(this.sendMessage, this.sendMessage.length, InetAddress.getByName(TFTP_SERVER_IP), TFTP_DEFAULT_PORT); 
            //On envoie notre requête
            this.datagramSocket.send(this.datagramPacketEmission);            
            //Par la suite, on ouvre notre fichier
            FileInputStream file = new FileInputStream(nameFile);
            //On déclare une variable pour le numero de bloc
            int blockNumber = 1;
            //On boucle jusqu'a la fin du fichier
            do
            {
                //On attends le ACK du serveur
                this.receivedMessage = new byte[PACKET_SIZE];
                this.datagramPacketReception = new DatagramPacket(this.receivedMessage, PACKET_SIZE);
                this.datagramSocket.receive(this.datagramPacketReception);
                //On analyse ce ACK
                byte[] opCode = {receivedMessage[0], receivedMessage[1]};
                //On check les erreurs
                String test = new String(this.datagramPacketReception.getData());
                System.out.println(test);
                if(opCode[0] == OP_ERROR)
                {
                    System.out.println("IL Y A EU ERREUR");
                }
                else //if(opCode[1] == OP_ACK)
                {
                    //On reçoit l'ack
                }
                //On créée la tête de paquet à envoyer
                byte[] head = new byte[4];
                head[0] = 0;
                head[1] = OP_DATAPACKET;
                head[2] = (byte) (blockNumber >> 8 & 0xFF);
                head[3] = (byte) (blockNumber & 0xFF);
                //On lit ensuite dans le fichier
                byte[] data = new byte[512];
                int length = file.read(data, 0, 512);
                //On copie la tête de paquet dans le tableau qu'on va envoyer
                this.sendMessage = new byte[length+4];
                System.arraycopy(head, 0, this.sendMessage, 0, head.length);
                //On copie ce qu'on a lu dans le tableau qu'on va envoyer
                System.arraycopy(data, 0, this.sendMessage, 4, length);
                this.datagramPacketEmission = new DatagramPacket(this.sendMessage, this.sendMessage.length, InetAddress.getByName(TFTP_SERVER_IP), this.datagramPacketReception.getPort());
                this.datagramSocket.send(datagramPacketEmission);
                blockNumber++;
                
            }while(this.sendMessage.length >= PACKET_SIZE);
            
            
        }catch (FileNotFoundException ex) {
            Logger.getLogger(TFTPClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SocketException ex) {
            Logger.getLogger(TFTPClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownHostException ex) {
            Logger.getLogger(TFTPClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TFTPClient.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    /**
     * Créer un packet d'aquitement en fonction du numero de block passé en paramètre
     * Ce packet est stocké dans la variable sendMessage de la classe.
     * @param ack_number Le numéro de block
     */
    public void CreateAckMessage(byte[] ack_number)
    {   
        this.sendMessage = new byte[4];
        this.sendMessage[1] = OP_ACK;
        this.sendMessage[2] = ack_number[0];
        this.sendMessage[3] = ack_number[1];
    }
    
    /**
     * Permet de créer un packet Request sous la convention TFTP
     * Met a jour la variable SendMessage de la classe.
     * @param op_code L'OP_CODE du packet
     * @param mode Le mode du packet (précisez "octet")
     * @param contenu Le contenu du packet (nom du fichier, ect)
     */
    public void CreateRequestMessage(byte op_code, String mode, String contenu)
    {
        this.sendMessage = new byte[2 + contenu.length() + 1 + mode.length() + 1];
        
        this.sendMessage[1] = op_code;
        for(int i=0; i<contenu.length(); i++)
        {
            this.sendMessage[i+2] = contenu.getBytes()[i];
        }
        for(int i=0; i<mode.length(); i++)
        {
            this.sendMessage[contenu.length()+3+i] = mode.getBytes()[i];
        }
        this.sendMessage[this.sendMessage.length-1] = 0;
    }

    /**
     * Vérifie si le paquet reçu est le dernier paquet.
     * @return Vrai si le dernier paquet reçu fait moins de 512 octets, faux sinon.
     */
    private boolean isLastPacket()
    {
        return this.datagramPacketReception.getLength() < 512;
    }
}
