package org.fys.controller;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.fys.utils.Token;

public class ConcurrencyManager {

    private static ConcurrencyManager _cmHandler;
    private final BlockingQueue<Token> _qvm;
    private final BlockingQueue<Token> _qmv;

    private final BlockingQueue<Token> _qvm2ctr;
    private boolean _synchronus;            

    //only a single instance of concurrencyManager is used throughout the lifespan of the application
    public static synchronized ConcurrencyManager getInstance()
    {
        if (_cmHandler == null){
            _cmHandler = new ConcurrencyManager();
        }
        return _cmHandler;
    }

    private ConcurrencyManager()
    {
        _qvm2ctr = new LinkedBlockingQueue<>();
        _qvm = new LinkedBlockingQueue<>();
        _qmv = new LinkedBlockingQueue<>();
        
        _synchronus = false;
    }

    //dispatch token to model
    public void dt2model(Token tk , int user) { 
        try {
            _qvm.put(tk);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(String.valueOf(user)+" Thread interrupted during sending a message for user: ", e);
        }
    }

    //listen to model
    public Token lt2model(int user) {
        try {
            return _qmv.take();
            } 
        catch (InterruptedException e) 
        {
            Thread.currentThread().interrupt();
            throw new RuntimeException(String.valueOf(user) + " Thread interrupted during receiving a message :", e);
        }
    }


    //dispatch token to controller
    public void dt2ctr(Token tk, int user)
    {
        try {
            _qvm2ctr.put(tk);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(String.valueOf(user) + " Thread interrupted during sending a message", e);
        }

    }

    //listen to view and model 
    public Token lt2vm( int user)
    {
        try {
            return _qvm2ctr.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(String.valueOf(user) + " Thread interrupted during sending a message", e);
        }

    }

    //dispatch token to view
    public void dt2view(Token tk, int user) {
        try {
            _qmv.put(tk);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(String.valueOf(user) + "Thread interrupted during sending a message", e);
        }
    }

    //listen to view
    public Token lt2view(int user) {
        try {
            return _qvm.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(String.valueOf(user) + "Thread interrupted during receiving a message", e);
    }
    }

    public boolean IsSynchronus(){
        return _synchronus;
    }

    public int InitializeSynchronization(){
        _synchronus = true;
        return 0;
        }
}
