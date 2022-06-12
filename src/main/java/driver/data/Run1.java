package driver.data;

import graphtheory.Structure;
import mytools.Config;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.sql.SQLException;


/**
 * A runner to process some insignificant work
 * @Author qkoqhh
 * @Date 2021-1-11
 */
public class Run1 {
    static String progress;
    public static void process(){
        if ("GenerateHubLabel".equals(progress)) {
            /**
             * Generate the hub label
             */
            GenerateHubLabel generator = new GenerateHubLabel(Config.database);
            generator.solve_sal();
            generator.solve_sd();
        }


        if ("TestHub".equals(progress)) {
            /**
             * Test if the hub label can calculate the correct distance
             */
            try {
                TestHub here = new TestHub(Config.database);
                here.source_sample_sd(100, 100000);
                here.source_sample_sal(100, 100000);
            } catch (Exception throwables) {
                throwables.printStackTrace();
            }
        }


        if ("GenFixQueryByRandom".equals(progress)) {
            /**
             * Generate Fix Query for lubm.
             * Just sample some nodes for a keyword
             */
            try {
                GenFixQueryByRandom here = new GenFixQueryByRandom(Config.database);
                here.gen();
            } catch (Exception throwables) {
                throwables.printStackTrace();
            }
        }

        if ("GenerateSubPedia".equals(progress)) {
            /**
             * Generate Sub DBPedia from dbpedia, and generate hub label
             */
            try {
                GenerateSubDBPedia here = new GenerateSubDBPedia("dbpedia");
//                Validator.comp(here.G);
                here.generate(1000);
                here.generate(50000);
                here.generate(6000000);
            } catch (Exception throwables) {
                throwables.printStackTrace();
            }
        }


        if ("ReadDBpedia".equals(progress)) {
            /**
             * Read DBpedia from raw file
             */
            ReadDBpedia here = new ReadDBpedia("dbpedia");
            final String pwd="";

            try {
                here.read_graph(pwd+"mappingbased_objects_en.ttl");
                here.set_vertex_weight();
                here.read_type(pwd+"instance_types_transitive_en.ttl");
                here.read_label(pwd+"labels_en.ttl");
                here.build_lucene();
                here.read_query(pwd+"dbpedia.queries-v2_stopped.txt");
                here.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if ("ReadLUBM".equals(progress)){
            /**
             * Read LUBM from raw file
             */
            String graph_name= Config.database;
            ReadLUBM here=new ReadLUBM(graph_name);
            try {
                System.out.println("Reading graph...");
                here.read_graph(graph_name+".nt");

                System.out.println("Setting vertex weight...");
                here.set_vertex_weight();

                System.out.println("Write in...");
                here.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {
        try {
            ArgumentParser parser = ArgumentParsers.newFor("Run1").build();
            parser.addArgument("-c", "--config").setDefault("config.properties").help("config file");
            parser.addArgument("-p", "--progress").setDefault("").help("progress name");
            Namespace ns = null;
            try {
                ns = parser.parseArgs(args);
            } catch (ArgumentParserException e) {
                parser.handleError(e);
                System.exit(1);
            }
            Config.config_filename = ns.get("config");
            progress = ns.get("progress");
            Config.init();
            if (Config.alpha_array.size() == 1) {
                Structure.alpha = Config.alpha_array.get(0);
            }

            process();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
