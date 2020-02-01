package gobgpapi.example;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.Message;
import com.google.protobuf.Any;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import gobgpapi.GobgpApiGrpc;
import gobgpapi.GobgpApiGrpc.GobgpApiStub;
import gobgpapi.GobgpApiGrpc.GobgpApiBlockingStub;
import gobgpapi.Gobgp;
import gobgpapi.Attribute;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

public class Client {
    private static final Logger logger = Logger.getLogger(Client.class.getName());
    private final GobgpApiBlockingStub blockingStub;
    private final GobgpApiStub newStub;
    public Client(Channel channel) {
        blockingStub = GobgpApiGrpc.newBlockingStub(channel);
        newStub = GobgpApiGrpc.newStub(channel);
    }
    private Gobgp.Family family(int ver) {
        return Gobgp.Family.newBuilder().setAfi(Gobgp.Family.Afi.AFI_IP).setSafi(Gobgp.Family.Safi.SAFI_FLOW_SPEC_UNICAST).build();
    }

    private List<Any> build_prefixes(List<Any> rules, int typeval, String ipv4addr) {
        Attribute.FlowSpecIPPrefix f1 = Attribute.FlowSpecIPPrefix.newBuilder().setType(typeval).setPrefixLen(32).setPrefix(ipv4addr).build();
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

        builder.addPattrs(Any.pack(Attribute.MpReachNLRIAttribute.newBuilder().setFamily(family).addNlris(nlri).addNextHops("0.0.1.0").build()));

        builder.addPattrs(Any.pack(Attribute.OriginAttribute.newBuilder().setOrigin(origin).build()));

        return builder;
    }


    public void process() {
        Gobgp.GetBgpResponse r = blockingStub.getBgp(Gobgp.GetBgpRequest.newBuilder().build());
      
        System.out.println(r.getGlobal());
        com.google.protobuf.Descriptors.Descriptor descriptor = r.getGlobal().getDescriptorForType();
        //for (com.google.protobuf.Descriptors.FieldDescriptor field : descriptor.getFields()) {
        //    System.out.println("field=" + field);
        //}
        //System.out.println(descriptor.getField(descriptor.findFieldByName("as")));
        //System.out.println(descriptor.getField("gobgpapi.Global.as"));
        Gobgp.Family family4 = family(4);

        List<Any> rules = new java.util.ArrayList<Any>();
        build_prefixes(rules, 1, "1.2.3.4");
        build_prefixes(rules, 2, "5.6.7.8");
        build_rules(rules, 3, new long[] {1, 17});
        build_rules(rules, 4, new long[] {1, 1900, 1, 11211});
        Any nlri = build_nlri(rules);

        Gobgp.Path.Builder path_builder = Gobgp.Path.newBuilder();
        path_builder.setFamily(family4);
        path_builder.setNlri(nlri);
        build_pattrs(path_builder, family4, nlri, 0, 0);
        path_builder.setSourceAsn(65001);

        Gobgp.Path path = path_builder.build();


        blockingStub.addPath(Gobgp.AddPathRequest.newBuilder().setTableType(Gobgp.TableType.GLOBAL).setPath(path).build());
        System.out.println(path);
        

    }

    public static void main(String[] args) throws InterruptedException {
        String target = "localhost:50051";
        ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        try {
            Client client = new Client(channel);
            System.out.println("new Client !!");
            client.process();
        }catch (Exception e){
        }
    }

}
