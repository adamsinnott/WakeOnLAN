package uk.co.verygoodapps.wakeonlan;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private EditText editTextAddress, editTextMacAddress;
    private Button buttonWakeUp;
    private TextView textViewState, textViewRx;

    UdpClientHandler udpClientHandler;
    UdpClientThread udpClientThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextAddress = (EditText) findViewById(R.id.address);
        editTextMacAddress = (EditText) findViewById(R.id.macaddress);
        buttonWakeUp = (Button) findViewById(R.id.wakeup);
        textViewState = (TextView) findViewById(R.id.state);
        textViewRx = (TextView) findViewById(R.id.received);

        buttonWakeUp.setOnClickListener(buttonConnectOnClickListener);

        udpClientHandler = new UdpClientHandler(this);
    }

    View.OnClickListener buttonConnectOnClickListener =
            new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {

                    udpClientThread = new UdpClientThread(
                            editTextAddress.getText().toString(),
                            editTextMacAddress.getText().toString(),
                            udpClientHandler);
                    udpClientThread.start();
//                    buttonWakeUp.setEnabled(false);
                }
            };

    private void updateState(String state) {
        textViewState.setText(state);
    }

    private void updateRxMsg(String rxmsg) {
        textViewRx.append(rxmsg + "\n");
    }

    private void clientEnd() {
        udpClientThread = null;
        textViewState.setText("clientEnd()");
        buttonWakeUp.setEnabled(true);
    }

    public static class UdpClientHandler extends Handler {
        public static final int UPDATE_STATE=0;
        public static final int UPDATE_MSG=1;
        public static final int UPDATE_END=2;
        private MainActivity parent;

        public UdpClientHandler(MainActivity parent) {
            super();
            this.parent = parent;
        }

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case UPDATE_STATE:
                    parent.updateState((String)msg.obj);
                    break;
                case UPDATE_MSG:
                    parent.updateRxMsg((String)msg.obj);
                    break;
                case UPDATE_END:
                    parent.clientEnd();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}
