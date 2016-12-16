import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLFile;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;
import uk.ac.ebi.pride.tools.mzxml_parser.mzxml.model.Peaks;
import uk.ac.ebi.pride.tools.mzxml_parser.mzxml.model.Scan;

import javax.xml.datatype.Duration;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by liuyanbo on 2016/12/16.
 */
public class Raw2Cluster {
    public static HashMap<Double, Double> MZ_RT = new HashMap();
    public static HashMap<Double, Double> RT_MZ = new HashMap<Double, Double>();
    public static HashMap<Double, Object> MZ_PeakList = new HashMap<Double, Object>();

    public static void main(String[] args){


        try {
            BufferedReader br = new BufferedReader(new FileReader("mz_rt_list"));
            String line = br.readLine();

            while ((line = br.readLine()) != null){
                String tmpPair[] = line.split("\t");
                MZ_RT.put(Double.valueOf(tmpPair[0]), Double.valueOf(tmpPair[1]));
                RT_MZ.put(Double.valueOf(tmpPair[1]), Double.valueOf(tmpPair[0]));
            }
            br.close();

            for (Double tmp: MZ_RT.keySet()){
                MZ_PeakList.put(tmp, new HashMap<Double, Double>());
            }

            MzXMLFile tmpParser = new MzXMLFile(new File("TTE-75-1-01-3.mzXML"));
            Iterator tmpIter = tmpParser.geMS1ScanIterator();

            while (tmpIter.hasNext()){
                Scan tmpScan = (Scan) tmpIter.next();

                String tmpRTS = tmpScan.getRetentionTime().toString();
                Pattern rtPattern = Pattern.compile("[0-9\\.]+");
                Matcher tmpMat = rtPattern.matcher(tmpRTS);
                if(tmpMat.find()){
                    Double tmpRT = Double.valueOf(tmpMat.group(0))/60;
                    for (Double tmp1:MZ_RT.values()){
                        if (rtSearch(tmp1, tmpRT)){
                            Map<Double, Double> peaksMap = MzXMLFile.convertPeaksToMap(tmpScan.getPeaks().get(0));
                            for (Double mz:peaksMap.keySet()){
                                    mzSearch(RT_MZ.get(tmp1), mz, peaksMap.get(mz), tmpRT);
                            }
                        }
                    }
                }
            }

            Integer tmpi1 = 1;
            for (Double tmp1: MZ_PeakList.keySet()){

                String tmpFileS = "Cluster" + tmpi1.toString();
                BufferedWriter bw = new BufferedWriter(new FileWriter(tmpFileS));
                String head = tmp1.toString() + "\t" + MZ_RT.get(tmp1).toString() +"\n\n";
                bw.write(head);
                for (Object tmp2: ((Map)MZ_PeakList.get(tmp1)).keySet()) {
                    String tmpLine = tmp2.toString() +"\t"+((Map)MZ_PeakList.get(tmp1)).get(tmp2).toString()+"\n";
                    bw.write(tmpLine);
                }
                bw.close();

                tmpi1++;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MzXMLParsingException e) {
            e.printStackTrace();
        }
    }

    //mzS is the mz which is searching for
    public static void mzSearch(Double mzS, Double mz, Double intensity, Double rt){
        if (Math.abs(mz-mzS) <= 0.05){
            Map tmpMap = (Map)MZ_PeakList.get(mzS);
            if (tmpMap.containsKey(rt)){
                tmpMap.put(rt,(((Double)tmpMap.get(rt))+intensity));
            }else {
                tmpMap.put(rt,intensity);
            }
        }
    }

    public static boolean rtSearch (Double rtS, Double rt){
        if (Math.abs(rt-rtS) <= 5 ){
            return true;
        }else {
            return false;
        }
    }
}
