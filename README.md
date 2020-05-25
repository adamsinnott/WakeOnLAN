# WakeOnLAN

This was a weekend project with the goal, to wake a computer up based on its MAC Address.

It was created with help from the following three resources:

* http://android-er.blogspot.com/2016/05/android-datagramudp-client-example.html
* http://www.jibble.org/wake-on-lan/
* https://www.baeldung.com/java-broadcast-multicast

It is essentially a button that sends a Datagram ["Magic Packet"](https://en.wikipedia.org/wiki/Wake-on-LAN#Magic_packet) to a Broadcast Address on destination Port 9. I was initially hopeful I could send it on Port 9 but Android seems to have this locked down and more investigation is required.

The Magic Packet is a broadcast frame containing anywhere within its payload 6 bytes of all 255 (FF FF FF FF FF FF in hexadecimal), followed by sixteen repetitions of the target computer's 48-bit MAC address, for a total of 102 bytes.

Magic Packets support passwords but this wasn't implemented. Additionally the device you are waking needs to be on the Physical LAN. Any wireless device would need Wireless Wake On LAN (see the wiki link). It may be this works as is but it hasn't been tested and would need the network card to support it.

On the target machine I have pre-emptively set up Wake On LAN in the BIOS then created this file in `/etc/systemd/system/wol.service`

```
[Unit]
Description=Configure Wake On LAN

[Service]
Type=oneshot
ExecStart=/sbin/ethtool -s <<interface name>> wol g

[Install]
WantedBy=basic.target
```

Then reload systemd, enable the service and start it
```
sudo systemctl daemon-reload
sudo systemctl enable wol.service
sudo systemctl start wol.service
```

See https://www.techrepublic.com/article/how-to-enable-wake-on-lan-in-ubuntu-server-18-04/ for more information.

Et voila, should all be up and running.

## Wireshark

If you want to look at your packets in Wireshark fire up Wireshark on your target machine (https://linuxhint.com/install_wireshark_ubuntu/), and use [the display filter `wol`](https://forums.ivanti.com/s/article/HOWTO-Troubleshoot-Wake-On-LAN-issues-with-Wireshark).

Send your packet from the app (I didn't have much luck with a VM as the virtual device is on a different network inside the host) and you should see something like the below.

```
Frame 8: 144 bytes on wire (1152 bits), 144 bytes captured (1152 bits) on interface 0
    Interface id: 0 (enp4s0)
    Encapsulation type: Ethernet (1)
    Arrival Time: May 25, 2020 20:09:25.201586908 BST
    [Time shift for this packet: 0.000000000 seconds]
    Epoch Time: 1590433765.201586908 seconds
    [Time delta from previous captured frame: 0.000007294 seconds]
    [Time delta from previous displayed frame: 0.000007294 seconds]
    [Time since reference or first frame: 6.278112172 seconds]
    Frame Number: 8
    Frame Length: 144 bytes (1152 bits)
    Capture Length: 144 bytes (1152 bits)
    [Frame is marked: False]
    [Frame is ignored: False]
    [Protocols in frame: eth:ethertype:ip:udp:wol]
    [Coloring Rule Name: UDP]
    [Coloring Rule String: udp]
Ethernet II, Src: 76:9c:20:73:63:8e (76:9c:20:73:63:8e), Dst: Broadcast (ff:ff:ff:ff:ff:ff)
    Destination: Broadcast (ff:ff:ff:ff:ff:ff)
    Source: 76:9c:20:73:63:8e (76:9c:20:73:63:8e)
    Type: IPv4 (0x0800)
Internet Protocol Version 4, Src: 192.168.0.158, Dst: 192.168.0.255
    0100 .... = Version: 4
    .... 0101 = Header Length: 20 bytes (5)
    Differentiated Services Field: 0x00 (DSCP: CS0, ECN: Not-ECT)
    Total Length: 130
    Identification: 0x3969 (14697)
    Flags: 0x4000, Don't fragment
    Time to live: 64
    Protocol: UDP (17)
    Header checksum: 0x7e14 [validation disabled]
    [Header checksum status: Unverified]
    Source: 192.168.0.158
    Destination: 192.168.0.255
User Datagram Protocol, Src Port: 33308, Dst Port: 9
    Source Port: 33308
    Destination Port: 9
    Length: 110
    Checksum: 0xbf6c [unverified]
    [Checksum Status: Unverified]
    [Stream index: 2]
Wake On LAN, MAC: Giga-Byt_9b:d5:de (b4:2e:99:9b:d5:de)
    Sync stream: ffffffffffff
    MAC: Giga-Byt_9b:d5:de (b4:2e:99:9b:d5:de)
        MAC: Giga-Byt_9b:d5:de (b4:2e:99:9b:d5:de)
        MAC: Giga-Byt_9b:d5:de (b4:2e:99:9b:d5:de)
        MAC: Giga-Byt_9b:d5:de (b4:2e:99:9b:d5:de)
        MAC: Giga-Byt_9b:d5:de (b4:2e:99:9b:d5:de)
        MAC: Giga-Byt_9b:d5:de (b4:2e:99:9b:d5:de)
        MAC: Giga-Byt_9b:d5:de (b4:2e:99:9b:d5:de)
        MAC: Giga-Byt_9b:d5:de (b4:2e:99:9b:d5:de)
        MAC: Giga-Byt_9b:d5:de (b4:2e:99:9b:d5:de)
        MAC: Giga-Byt_9b:d5:de (b4:2e:99:9b:d5:de)
        MAC: Giga-Byt_9b:d5:de (b4:2e:99:9b:d5:de)
        MAC: Giga-Byt_9b:d5:de (b4:2e:99:9b:d5:de)
        MAC: Giga-Byt_9b:d5:de (b4:2e:99:9b:d5:de)
        MAC: Giga-Byt_9b:d5:de (b4:2e:99:9b:d5:de)
        MAC: Giga-Byt_9b:d5:de (b4:2e:99:9b:d5:de)
        MAC: Giga-Byt_9b:d5:de (b4:2e:99:9b:d5:de)
        MAC: Giga-Byt_9b:d5:de (b4:2e:99:9b:d5:de)

```

