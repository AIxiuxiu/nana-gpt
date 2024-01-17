package com.website.util;

import com.google.common.collect.ImmutableList;
import com.website.config.MilvusConfig;
import com.website.entity.DataSqlEntity;
import io.milvus.client.MilvusServiceClient;
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import io.milvus.grpc.*;
import io.milvus.param.*;
import io.milvus.param.collection.*;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.param.index.DropIndexParam;
import io.milvus.param.partition.*;
import io.milvus.response.GetCollStatResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: ahl
 */
@Slf4j
@Component
public class MilvusClientUtil {

    @Autowired
    private MilvusConfig milvusConfig;


    private static MilvusConfig stConfig;
    /**
     * 使用milvus向量数据库
     */
    private static  MilvusServiceClient milvusClient;

    @PostConstruct
    private void init(){
        stConfig=milvusConfig;
        milvusClient = new MilvusServiceClient(
                ConnectParam.newBuilder()
                        .withHost(stConfig.getIp())
                        .withPort(stConfig.getPort())
                        .build());
    }

    private MilvusClientUtil(){}

    // nprobe 可以 Search 时调整搜索的数据量，nprobe 越大，recall 越高，但性能越差
    public static final String SEARCH_PARAM = "{\"nprobe\":64}";
    public static final Integer VECTOR_DIM = 1536;

    //字段
    public static final String ID_FIELD = "docID";
    public static final String INDEX_NAME = "docEmbedIndex";
    public static final String VECTOR_FIELD = "docEmbed";

    // 字典树索引
    public static final IndexType INDEX_TYPE = IndexType.IVF_FLAT;
    //一般建议 nlist = 4*sqrt(N)，对于 Milvus 而言，一个 Segment 默认是 512M 数据，对于 128dim 向量而言，一个 segment 包含 100w 数据，因此最佳 nlist 在 1000 左右
    public static final String INDEX_PARAM = "{\"nlist\":1024}";
    // 检索类型为内积，也可换成欧氏：MetricType.L2
    public static final MetricType METRIV_TYPE = MetricType.IP;

    // 分片是指将写操作分配给不同的节点
    public static final Integer SHARDS_NUM = 2;


    public static MilvusServiceClient getMilvusClient() {
        return milvusClient;
    }


    /**
     *  创建合集
     */
    public static R<RpcStatus> createCollection(String collectionName) {
        if (hasCollection(collectionName)) {
            log.warn("合集{}已存在", collectionName);
            return null;
        }
        FieldType id = FieldType.newBuilder()
                .withName("ID")
                .withDescription("doc identification")
                .withDataType(DataType.Int64)
                .withPrimaryKey(true)
                .withAutoID(true)
                .build();

        FieldType docId = FieldType.newBuilder()
                .withName(ID_FIELD)
                .withDescription("doc identification")
                .withDataType(DataType.VarChar)
                .withMaxLength(32)
                .build();

        FieldType docEmbedding = FieldType.newBuilder()
                .withName(VECTOR_FIELD)
                .withDescription("doc embedding")
                .withDataType(DataType.FloatVector)
                .withDimension(VECTOR_DIM)
                .build();

        CreateCollectionParam createCollectionReq = CreateCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .withDescription("doc info")
                .withShardsNum(SHARDS_NUM)
                .addFieldType(id)
                .addFieldType(docId)
                .addFieldType(docEmbedding)
                .build();
        R<RpcStatus> response = milvusClient.createCollection(createCollectionReq);
        handleResponseStatus(response);
        return response;
    }

    /**
     * 创建分区
     * @param collectionName
     * @param partitionName
     * @return
     */
    public static R<RpcStatus> createPartition(String collectionName, String partitionName) {
        if (hasPartitionName(collectionName, partitionName)) {
            log.warn("合集{}的分区{}已存在", collectionName, partitionName);
            return null;
        }
        R<RpcStatus> response = milvusClient.createPartition(
                CreatePartitionParam.newBuilder()
                        .withCollectionName(collectionName)
                        .withPartitionName(partitionName)
                        .build());
        handleResponseStatus(response);
        return response;
    }


    /**
     * 生成索引-建议创建合集后直接创建索引-方便后续系统自动建立
     * @param collectionName
     * @return
     */
    public static R<RpcStatus> createIndex(String collectionName) {
        R<RpcStatus> response =  milvusClient.createIndex(CreateIndexParam.newBuilder()
                .withCollectionName(collectionName)
                .withFieldName(VECTOR_FIELD)
                .withIndexName(INDEX_NAME)
                .withIndexType(INDEX_TYPE)
                .withMetricType(METRIV_TYPE)
                .withExtraParam(INDEX_PARAM)
                .withSyncMode(Boolean.TRUE)
                .build());
        handleResponseStatus(response);
        return response;
    }


    /**
     * 判断是否有了这个合集
     * @param collectionName
     * @return
     */
    public static boolean hasCollection(String collectionName) {
        R<Boolean> response = milvusClient.hasCollection(HasCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .build());
        handleResponseStatus(response);
        return response.getData();
    }

    /**
     * 判断是否有了这个分区
     * @param collectionName
     * @param partitionName
     * @return
     */
    public static boolean hasPartitionName(String collectionName, String partitionName) {
        R<Boolean> response = milvusClient.hasPartition(HasPartitionParam.newBuilder()
                .withCollectionName(collectionName)
                .withPartitionName(partitionName)
                .build());
        handleResponseStatus(response);
        return response.getData();
    }

    public static boolean getLoadState(String collectionName) {
        R<GetLoadStateResponse> response = milvusClient.getLoadState(GetLoadStateParam.newBuilder()
                .withCollectionName(collectionName)
                .build());
        if (response.getStatus() == R.Status.Success.getCode()) {
            return true;
        }
        return false;
    }

    /**
     * 加载合集
     * @param collectionName
     * @return
     */
    public static R<RpcStatus> loadCollection(String collectionName) {
        R<RpcStatus> response = milvusClient.loadCollection(LoadCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .build());
        handleResponseStatus(response);
        return response;
    }

    /**
     * 加载合集的分区
     * @param collectionName
     * @param partitionNames
     * @return
     */
    public static R<RpcStatus> loadPartitions(String collectionName, List<String> partitionNames) {
        R<RpcStatus> response = milvusClient.loadPartitions( LoadPartitionsParam.newBuilder()
                        .withCollectionName(collectionName)
                        .withPartitionNames(partitionNames)
                .build());
        handleResponseStatus(response);
        return response;
    }

    /**
     *  将文章向量插入向量库中
     */
    public static R<MutationResult> insert(DataSqlEntity dataSqlEntity, String collectionName, String partitionName) {
        if (StringUtils.isEmpty(partitionName)) {
            // 默认分区
            partitionName = "_default";
        }
        List<InsertParam.Field> fields = new ArrayList<>();
        fields.add(new InsertParam.Field(ID_FIELD, dataSqlEntity.getDocId()));
        fields.add(new InsertParam.Field(VECTOR_FIELD, dataSqlEntity.getLl()));

        InsertParam insertParam = InsertParam.newBuilder()
                .withCollectionName(collectionName)
                .withPartitionName(partitionName)
                .withFields(fields)
                .build();

        R<MutationResult> response = milvusClient.insert(insertParam);
        handleResponseStatus(response);
        return response;
    }

    /**
     * 释放合集
     * @param collectionName
     * @return
     */
    public static R<RpcStatus> releaseCollection(String collectionName) {
        R<RpcStatus> response = milvusClient.releaseCollection(ReleaseCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .build());
        handleResponseStatus(response);
        return response;
    }


    /**
     * 释放合集的分片
     * @param collectionName
     * @param partitionNames
     * @return
     */
    public static R<RpcStatus> releasePartitions(String collectionName, List<String> partitionNames) {
        R<RpcStatus> response = milvusClient.releasePartitions(ReleasePartitionsParam.newBuilder()
                .withCollectionName(collectionName)
                .withPartitionNames(partitionNames)
                .build());
        handleResponseStatus(response);
        return response;
    }


    /**
     * 删除合集
     * @param collectionName
     * @return
     */
    public static R<RpcStatus> dropCollection(String collectionName) {
        R<RpcStatus> response = milvusClient.dropCollection(DropCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .build());
        handleResponseStatus(response);
        return response;
    }

    /**
     * 删除索引
     * @param collectionName
     * @return
     */
    public static R<RpcStatus> dropIndex(String collectionName) {
        R<RpcStatus> response = milvusClient.dropIndex(DropIndexParam.newBuilder()
                .withCollectionName(collectionName)
                .withIndexName(MilvusClientUtil.INDEX_NAME)
                .build());
        handleResponseStatus(response);
        return response;
    }



    /**
     * 删除合集的分片
     * @param collectionName
     * @param partitionName
     * @return
     */
    public static R<RpcStatus> dropPartitions(String collectionName, String partitionName) {
        R<RpcStatus> response = milvusClient.dropPartition(DropPartitionParam.newBuilder()
                .withCollectionName(collectionName)
                .withPartitionName(partitionName)
                .build());
        handleResponseStatus(response);
        return response;
    }

    /**
     * 搜索相似文档
     * @param vectors
     * @param collectionName
     * @return
     */
    public static R<SearchResults> searchContent(List<List<Float>> vectors, String collectionName, int topK) {
        long begin = System.currentTimeMillis();
        SearchParam searchParam = SearchParam.newBuilder()
                .withCollectionName(collectionName)
                .withConsistencyLevel(ConsistencyLevelEnum.STRONG)
                .withMetricType(MetricType.IP)
                .withOutFields(ImmutableList.of(ID_FIELD))
                .withTopK(topK)
                .withVectors(vectors)
                .withVectorFieldName(VECTOR_FIELD)
                .withParams(SEARCH_PARAM)
                .withGuaranteeTimestamp(Constant.GUARANTEE_EVENTUALLY_TS)
                .build();

        R<SearchResults> response = milvusClient.search(searchParam);
        long end = System.currentTimeMillis();
        long cost = (end - begin);
        log.info("search time cost: {}", cost);
        handleResponseStatus(response);
        return response;
    }

    /**
     * 搜索相似文档
     * @param vectors
     * @param collectionName
     * @param partitionNames
     * @return
     */
    public static R<SearchResults> searchContent(List<List<Float>> vectors, String collectionName, List<String> partitionNames, int topK) {
        loadPartitions(collectionName, partitionNames);
        long begin = System.currentTimeMillis();
        SearchParam searchParam = SearchParam.newBuilder()
                .withCollectionName(collectionName)
                .withPartitionNames(partitionNames)
                .withConsistencyLevel(ConsistencyLevelEnum.STRONG)
                .withMetricType(MetricType.IP)
                .withOutFields(ImmutableList.of(ID_FIELD))
                .withTopK(topK)
                .withVectors(vectors)
                .withVectorFieldName(VECTOR_FIELD)
                .withParams(SEARCH_PARAM)
                .withGuaranteeTimestamp(Constant.GUARANTEE_EVENTUALLY_TS)
                .build();

        R<SearchResults> response = milvusClient.search(searchParam);
        long end = System.currentTimeMillis();
        long cost = (end - begin);
        log.info("search time cost: {}", cost);
        handleResponseStatus(response);
        releasePartitions(collectionName, partitionNames);
        return response;
    }

    public static void handleResponseStatus(R<?> r) {
        if (r.getStatus() != R.Status.Success.getCode()) {
            throw new RuntimeException(r.getMessage());
        }
    }

    // 统计当前连接的数据条目
    public static R<GetCollectionStatisticsResponse> getCollectionStatistics(String collectionName) {
        milvusClient.flushAll(true, 1000, 5000);

        R<GetCollectionStatisticsResponse> response = milvusClient.getCollectionStatistics(
                GetCollectionStatisticsParam.newBuilder()
                        .withCollectionName(collectionName)
                        .build());
        handleResponseStatus(response);
        GetCollStatResponseWrapper wrapper = new GetCollStatResponseWrapper(response.getData());
        System.out.println("Collection row count: " + wrapper.getRowCount());
        return response;
    }
}
