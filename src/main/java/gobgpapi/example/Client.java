package gobgpapi.example;

import com.google.protobuf.Any;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import gobgpapi.GobgpApiGrpc;
//import gobgpapi.GobgpApiGrpc.GobgpApiStub;
import gobgpapi.GobgpApiGrpc.GobgpApiBlockingStub;
import gobgpapi.Gobgp;
import gobgpapi.Attribute;
import java.util.List;

public class Client {

    private final GobgpApiBlockingStub blockingStub;

    //private final GobgpApiStub newStub;

    public Client(Channel channel) {
        blockingStub = GobgpApiGrpc.newBlockingStub(channel);
        //newStub = GobgpApiGrpc.newStub(channel);
    }

    private Gobgp.Family family(int ver) {
        return Gobgp.Family.newBuilder().setAfi((ver==4) ? Gobgp.Family.Afi.AFI_IP : Gobgp.Family.Afi.AFI_IP6).setSafi(Gobgp.Family.Safi.SAFI_FLOW_SPEC_UNICAST).build();
    }

    private List<Any> build_prefixes(List<Any> rules, int typeval, String ipaddr, int prefixlen) {
        Attribute.FlowSpecIPPrefix f1 = Attribute.FlowSpecIPPrefix.newBuilder().setType(typeval).setPrefixLen(prefixlen).setPrefix(ipaddr).build();
        rules.add(Any.pack(f1));
        return rules;
    }

    private List<Any> build_rules(List<Any> rules, int typeval, long[] conditions) {
        Attribute.FlowSpecComponent.Builder builder = Attribute.FlowSpecComponent.newBuilder();
        builder.setType(typeval);
        for(int i=0; i<conditions.length/2; i++){
            int op = (int) conditions[i*2];
            long val = conditions[i*2+1];
            Attribute.FlowSpecComponentItem item = Attribute.FlowSpecComponentItem.newBuilder().setOp(op).setValue(val).build();
            builder.addItems(item);
        }
        rules.add(Any.pack(builder.build()));
        return rules;
    }

    private Any build_nlri(List<Any> a){
        Attribute.FlowSpecNLRI.Builder builder = Attribute.FlowSpecNLRI.newBuilder();
        for(Any e : a){
            builder.addRules(e);
        }
        Any o = Any.pack(builder.build());
        return o;
    }

    private Attribute.ExtendedCommunitiesAttribute build_action(int rate) {
        Any o = Any.pack(Attribute.TrafficRateExtended.newBuilder().setRate((float) rate).build());// as = 0
        return Attribute.ExtendedCommunitiesAttribute.newBuilder().addCommunities(o).build();
    }

    private Gobgp.Path.Builder build_pattrs(Gobgp.Path.Builder builder, Gobgp.Family family, Any nlri, int rate, int origin) {
        builder.addPattrs(Any.pack(build_action(rate)));

        builder.addPattrs(Any.pack(Attribute.MpReachNLRIAttribute.newBuilder().setFamily(family).addNlris(nlri).addNextHops("0.0.0.0").build()));

        builder.addPattrs(Any.pack(Attribute.OriginAttribute.newBuilder().setOrigin(origin).build()));

        return builder;
    }

    private Gobgp.Path build_path() {
        // family
        Gobgp.Family family4 = family(4);

        // retrive BgpResponse
        Gobgp.GetBgpResponse r = blockingStub.getBgp(Gobgp.GetBgpRequest.newBuilder().build());
        int asn = r.getGlobal().getAs();

        // make nlri
        List<Any> rules = new java.util.ArrayList<Any>();

        build_prefixes(rules, 1, "1.2.3.4", 32);
        build_prefixes(rules, 2, "5.6.7.8", 32);
        build_rules(rules, 3, new long[] {1, 17});
        build_rules(rules, 4, new long[] {1, 1900, 1, 11211});
        Any nlri = build_nlri(rules);

        // make path
        Gobgp.Path.Builder path_builder = Gobgp.Path.newBuilder();
        path_builder.setFamily(family4);
        path_builder.setNlri(nlri);
        build_pattrs(path_builder, family4, nlri, 0, 0);
        path_builder.setSourceAsn(asn);

        return path_builder.build();
    }

    public void run_addPath(Gobgp.Path path) {
        blockingStub.addPath(Gobgp.AddPathRequest.newBuilder().setTableType(Gobgp.TableType.GLOBAL).setPath(path).build());
    }

    public void run_delPath(Gobgp.Path path) {
        blockingStub.deletePath(Gobgp.DeletePathRequest.newBuilder().setTableType(Gobgp.TableType.GLOBAL).setPath(path).build());
    }

    private static final String target = "localhost:50051";

    public static void process(String cmd) {
        if (!"add".equals(cmd) && !"del".equals(cmd)){
            System.out.println("indicate 'add' or 'del'");
            return;
        }
        ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        try {
            Client client = new Client(channel);
            if("add".equals(cmd)){
                System.out.println("call addPath");
                client.run_addPath(client.build_path());
            }else{
                System.out.println("call delPath");
                client.run_delPath(client.build_path());
            }
            System.out.println("Done");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        process("add"); // addPath
        // process("del"); // delPath
        System.out.println("done");
    }

}
