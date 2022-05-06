
 void FIRFilter(float** dataOut, float** dataIn, int Row, int Col, int timeWindow,
	int filterLenght, int filterType, int windowType, double lowFreq, double highFreq);

 void FIRFilterSingle(float* dataOut, float* dataIn, int Row, int timeWindow,
	int filterLenght, int filterType, int windowType, double lowFreq, double highFreq);

 void ZDZY(int record_len1,int *datain, int antennaType, int segmentedGainMax, int *FANGDA1,int *multinum1,int &isEnd);

 void CorrectZeroOffset(float *m_datasBuf,float *m_datasBufProc,int m_scanLen,int antennaFreq,int timeWindow);