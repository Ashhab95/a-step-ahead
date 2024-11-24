
package org.fys.view;

import javafx.application.Application;

public class ViewPort implements Runnable {

    @Override
    public void run() {
        
        Application.launch(UIComponents.class);
    }

    public ViewPort(){}
}
