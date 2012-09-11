// $Id: ErrMgrC.nc,v 1.4 2006/05/18 19:58:40 chien-liang Exp $

configuration LedsBlinkerTesterC {
}
implementation {
	components MainC, LedsBlinkerC, LedsBlinkerTesterP;
	
	LedsBlinkerTesterP.Boot -> MainC;
	LedsBlinkerTesterP.LedsBlinker -> LedsBlinkerC;
}
