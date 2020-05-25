package uk.co.verygoodapps.wakeonlan;

import android.os.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

public class UdpClientThread extends Thread{

    private String dstAddress, dstMacAddress;
    MainActivity.UdpClientHandler handler;

    DatagramSocket socket;
    InetAddress address;
    public static final int PORT = 9;

    public UdpClientThread(String addr, String macaddress, MainActivity.UdpClientHandler handler) {
        super();
        dstAddress = addr;
        dstMacAddress = macaddress;
        this.handler = handler;
    }

    private void sendState(String state){
        handler.sendMessage(
                Message.obtain(handler,
                        MainActivity.UdpClientHandler.UPDATE_STATE, state));
    }


    private List<InetAddress> listAllBroadcastAddresses() throws SocketException {
        List<InetAddress> broadcastList = new ArrayList<>();
        Enumeration<NetworkInterface> interfaces
                = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();

            if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                continue;
            }

            networkInterface.getInterfaceAddresses().stream()
                    .map(a -> a.getBroadcast())
                    .filter(Objects::nonNull)
                    .forEach(broadcastList::add);
        }
        return broadcastList;
    }

    @Override
    public void run() {
        sendState("Waking your pc up...");

        try {
            List<InetAddress> listOfBroadcastAddresses = listAllBroadcastAddresses();
            System.out.println("List of broadcast addresses");
            System.out.println(listOfBroadcastAddresses);

            byte[] macBytes = getMacBytes(dstMacAddress);
            byte[] bytes = new byte[6 + 16 * macBytes.length];
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) 0xff;
            }
            for (int i = 6; i < bytes.length; i += macBytes.length) {
                System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
            }

            InetAddress address = InetAddress.getByName(dstAddress);

            // first do the direct packet sending to the address specified
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, PORT);
            DatagramSocket socket = new DatagramSocket();
            socket.send(packet);
            socket.close();

            // now do it to the broadcast addresses (although likely only one)
            for (int i=0; i< listOfBroadcastAddresses.size(); i++){
                InetAddress broadcastAddress = listOfBroadcastAddresses.get(i);
                System.out.println(broadcastAddress);
                packet = new DatagramPacket(bytes, bytes.length, broadcastAddress, PORT);
                socket = new DatagramSocket();
                socket.send(packet);
                socket.close();
            }

            String line = "Wake-on-LAN packet sent.";

            handler.sendMessage(
                    Message.obtain(handler, MainActivity.UdpClientHandler.UPDATE_MSG, line));

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(socket != null){
                socket.close();
                handler.sendEmptyMessage(MainActivity.UdpClientHandler.UPDATE_END);
            }
        }

    }

    private static byte[] getMacBytes(String macStr) throws IllegalArgumentException {
        byte[] bytes = new byte[6];
        String[] hex = macStr.split("(\\:|\\-)");
        if (hex.length != 6) {
            throw new IllegalArgumentException("Invalid MAC address.");
        }
        try {
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) Integer.parseInt(hex[i], 16);
            }
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex digit in MAC address.");
        }
        return bytes;
    }
}