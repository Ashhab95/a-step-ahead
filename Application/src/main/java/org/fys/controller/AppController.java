package org.fys.controller;

import org.fys.utils.Token;
import org.fys.view.ViewPort;
import org.w3c.dom.css.ViewCSS;
import org.fys.models.Model;

public class AppController {

    private ConcurrencyManager _cmHandler;

    private Thread  _vpThread;
    private ViewPort  _vPort;

    private Thread  _modelThread;
    private Model   _model;

    private Token _tk;

    public void run()
    {
        loadUIComponents();
        loadModels();
        InitializeSynchronizationSequence();

       _tk = _cmHandler.lt2vm(Token.CTR_ID);
        if( _tk.read() == Token.MODEL_ON || _tk.read() == Token.VIEW_ON)
        {
            _tk = _cmHandler.lt2vm(Token.CTR_ID);
            if( _tk.read() == Token.MODEL_ON || _tk.read() == Token.VIEW_ON)
            {
                rSleep();
                return;
            }
            else
            {
                _vpThread.interrupt();
                _modelThread.interrupt();
                
                return;
            }
        }
        else
        {
            _vpThread.interrupt();
            _modelThread.interrupt();
            
            return;
        }

    }
    private void rSleep()
    {
        _tk = _cmHandler.lt2vm(Token.CTR_ID);
        if (_tk.read() == Token.MODEL_BRK || _tk.read() == Token.VIEW_BRK)
        {
            _vpThread.interrupt();
            _modelThread.interrupt();
        }
        return;
    }

    public AppController()
    {
        _vPort   = new ViewPort();
        _vpThread = new Thread(_vPort);

        _model    = new Model();
        _modelThread = new Thread(_model);

        _cmHandler = ConcurrencyManager.getInstance();
    
    }

    public void loadUIComponents()
    {

        _vpThread.start();

    }
    public void loadModels()
    {
        _modelThread.start();
    }

    public void InitializeSynchronizationSequence()
    {
        _cmHandler.InitializeSynchronization();
    }

}
