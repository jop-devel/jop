package edu.purdue.scjtck.tck;

import edu.purdue.scjtck.MainSafelet;

public abstract class TestCase extends MainSafelet {

//    private String _name = getClass().getName();
	private String _name = "NYI";
    private boolean _passed = true; 
    private int _freeSlot;
    private final int _MAX_MESSAGES_PER_TESTCASE = 4;
    private String[] _errorMessages = new String[_MAX_MESSAGES_PER_TESTCASE + 1];

    protected String getInfo() {
        String info = "";

        info += "*********************************\n";
        info += " " + _name + "\n";
        info += " MissionMem: " + _prop._missionMemSize + "\n"; 
//        info += " " + getLevel() + "\tPriority " + _prop._priority + "\n";
        info += " Priority " + _prop._priority + "\n";
        info += " Period " + _prop._period + "\tDuration " + _prop._duration
                + "\n";
        info += "*********************************";

        return info;
    }

    protected String report() {
    	
        String result = "";
       
        if (!_passed) {
            result += "Failed:\n";
            for (int i = 0; i < _freeSlot; i++){
            	result += " [" + i + "] " + _errorMessages[i] + "\n";
            }
                
        }
        return result;
    }

    void fail(final String why) {
        _passed = false;
        if (_freeSlot <= _MAX_MESSAGES_PER_TESTCASE) {
            if (_freeSlot == _MAX_MESSAGES_PER_TESTCASE)
                _errorMessages[_freeSlot++] = "No more error messages can be recorded";
            else {
                int i;
                // do not record the same failure
                for (i = 0; i < _freeSlot; i++)
                    if (_errorMessages[i].equals(why))
                        break;
                if (i == _freeSlot)
                    _errorMessages[_freeSlot++] = why;
            }
        }
    }
}
