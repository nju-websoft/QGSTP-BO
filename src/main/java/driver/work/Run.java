package driver.work;

import graphtheory.Structure;
import mytools.Config;
import mytools.Info;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

import static graphtheory.Structure.alpha;

public class Run {
    public static void process() {
        int count= Config.query_num;
        String resultfile=Config.result_file;
        List<Double> alpha_array=Config.alpha_array;

        try {
            WorkerBase work=null;
            if(Config.worker==1){
                work = new Worker(Config.database);
            }else if(Config.worker==2){
                work = new Worker2(Config.database);
            }else if(Config.worker==3){
                work = new Worker3(Config.database);
            }
            work.read_data();
            // Don't use PrintWrite here, because it will lose all the data if the process is interrupted.
            PrintStream out=new PrintStream(resultfile);
            out.println("----------------------------------------");
            Info info=new Info();
            if(Config.record_each_query) {
                info.init();
            }
            for(Double enum_alpha:alpha_array) {
                work.reset_alpha(enum_alpha);
                info.alpha=enum_alpha;

                double time_EO = 0, time_QO = 0, time_B3F = 0, time_BO =0,time_DPBF=0;
                double rate_EO = 0, rate_QO = 0, rate_BO =0;
                RetInfo info_EO = null, info_QO = null, info_B3F = null, info_BO =null,info_DPBF=null;
                for (int i = 0; i < count; i++) {
                    System.out.println("Expr " + i + ":");
                    work.gen_keyword(i);
                    info.data_id=i;
                    info.g=work.Q.g;

                    if (Config.algorithm_EO) {
                        work.reset_algo(Algorithm_type.EO);
                        info_EO = work.run_algorithm();
                        time_EO += info_EO.time;
                    }

                    if (Config.algorithm_BO) {
                        work.reset_algo(Algorithm_type.BO);
                        info_BO = work.run_algorithm();
                        time_BO += info_BO.time;
                    }

                    if (Config.algorithm_QO) {
                        work.reset_algo(Algorithm_type.QO);
                        info_QO = work.run_algorithm();
                        time_QO += info_QO.time;
                    }

                    if (Config.algorithm_B3F) {
                        work.reset_algo(Algorithm_type.B3F);
                        info_B3F = work.run_algorithm();
                        time_B3F += info_B3F.time;
                    }

                    if (Config.algorithm_DPBF) {
                        work.reset_algo(Algorithm_type.DPBF);
                        info_DPBF = work.run_algorithm();
                        time_DPBF += info_DPBF.time;
                    }




                    if (Config.algorithm_B3F && Config.algorithm_EO) {
                        rate_EO += info_EO.ans.cost / info_B3F.ans.cost;
                    }
                    if (Config.algorithm_B3F && Config.algorithm_BO) {
                        rate_BO += info_BO.ans.cost / info_B3F.ans.cost;
                    }
                    if (Config.algorithm_B3F && Config.algorithm_QO) {
                        rate_QO += info_QO.ans.cost / info_B3F.ans.cost;
                    }
                    if(Config.record_each_query) {
                        if (Config.algorithm_EO) {
                            info.algorithm="EO";
                            info.runtime=info_EO.time;
                            info.cost=info_EO.ans.cost;
                            info.anstree=new LinkedList<>();
                            info.anstree.addAll(info_EO.ans.map.keySet());
                            info.add();
                        }

                        if (Config.algorithm_BO) {
                            info.algorithm="BO";
                            info.runtime= info_BO.time;
                            info.cost= info_BO.ans.cost;
                            info.anstree=new LinkedList<>();
                            info.anstree.addAll(info_BO.ans.map.keySet());
                            info.add();
                        }

                        if (Config.algorithm_QO) {
                            info.algorithm="QO";
                            info.runtime=info_QO.time;
                            info.cost=info_QO.ans.cost;
                            info.anstree=new LinkedList<>();
                            info.anstree.addAll(info_QO.ans.map.keySet());
                            info.add();
                        }

                        if(Config.algorithm_B3F){
                            info.algorithm="B3F";
                            info.runtime=info_B3F.time;
                            info.cost=info_B3F.ans.cost;
                            info.anstree=new LinkedList<>();
                            info.anstree.addAll(info_B3F.ans.map.keySet());
                            info.add();
                        }

                        if(Config.algorithm_DPBF){
                            info.algorithm="DPBF";
                            info.runtime=info_DPBF.time;
                            info.cost=info_DPBF.ans.cost;
                            info.anstree=new LinkedList<>();
                            info.anstree.addAll(info_DPBF.ans.map.keySet());
                            info.add();
                        }
                    }
                }
                time_EO /= count;
                rate_EO /= count;
                time_BO /= count;
                rate_BO /= count;
                time_QO /= count;
                rate_QO /= count;
                time_B3F /= count;
                time_DPBF /= count;
                out.println("alpha=" + alpha);
                if(Config.algorithm_EO) {
                    out.println("algorithm: EO");
                    out.println("Time: " + time_EO + " s");
                    if(Config.algorithm_B3F) {
                        out.println("Rate: " + rate_EO);
                    }
                }
                out.println("");
                if(Config.algorithm_BO) {
                    out.println("algorithm: BO");
                    out.println("Time: " + time_BO + " s");
                    if(Config.algorithm_B3F) {
                        out.println("Rate: " + rate_BO);
                    }
                }
                out.println("");
                if(Config.algorithm_QO) {
                    out.println("algorithm: QO");
                    out.println("Time: " + time_QO + " s");
                    if(Config.algorithm_B3F) {
                        out.println("Rate: " + rate_QO);
                    }
                }
                out.println("");
                if(Config.algorithm_B3F) {
                    out.println("algorithm: B3F");
                    out.println("Time: " + time_B3F + " s");
                }
                out.println("");
                if(Config.algorithm_DPBF) {
                    out.println("algorithm: DPBF");
                    out.println("Time: " + time_DPBF + " s");
                }
                out.println("----------------------------------------");
            }
            out.close();
            work.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            ArgumentParser parser= ArgumentParsers.newFor("Run").build();
            parser.addArgument("-c","--config").setDefault("config.properties").help("config file");
            Namespace ns=null;
            try{
                ns=parser.parseArgs(args);
            } catch (ArgumentParserException e) {
                parser.handleError(e);
                System.exit(1);
            }
            Config.config_filename=ns.get("config");
            Config.init();
            if (Config.alpha_array.size() == 1) {
                Structure.alpha = Config.alpha_array.get(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        process();
    }

}
