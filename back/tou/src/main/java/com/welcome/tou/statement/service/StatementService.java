package com.welcome.tou.statement.service;

import com.welcome.tou.client.domain.Branch;
import com.welcome.tou.client.domain.BranchRepository;
import com.welcome.tou.client.domain.Worker;
import com.welcome.tou.client.domain.WorkerRepository;
import com.welcome.tou.common.exception.InvalidTradeException;
import com.welcome.tou.common.exception.MismatchException;
import com.welcome.tou.common.exception.NotFoundException;
import com.welcome.tou.common.utils.ResultTemplate;
import com.welcome.tou.statement.domain.*;
import com.welcome.tou.statement.dto.request.RefuseStatementRequestDto;
import com.welcome.tou.statement.dto.request.SignStatementRequestDto;
import com.welcome.tou.statement.dto.request.StatementCreateRequestDto;
import com.welcome.tou.statement.dto.response.*;
import com.welcome.tou.stock.domain.Stock;
import com.welcome.tou.stock.domain.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatementService {

    private final StatementRepository statementRepository;
    private final ItemRepository itemRepository;
    private final BranchRepository branchRepository;
    private final StockRepository stockRepository;
    private final WorkerRepository workerRepository;
    private final StatementQueryRepository statementQueryRepository;


    public ResultTemplate getTradeCountList(UserDetails worker, Long branchSeq) {
        Long workerSeq = Long.parseLong(worker.getUsername());
        Worker reqWorker = workerRepository.findById(workerSeq)
                .orElseThrow(() -> new NoSuchElementException("요청 유저를 찾을 수 없습니다."));

        Branch branch = branchRepository.findById(branchSeq).orElseThrow(() -> {
            throw new NotFoundException(NotFoundException.BRANCH_NOT_FOUND);
        });

        if (reqWorker.getCompany().getCompanySeq() != branch.getCompany().getCompanySeq()) {
            throw new MismatchException(MismatchException.WORKER_AND_BRANCH_MISMATCH);
        }

        List<BranchTradeCountResponseDto> response;
        if (reqWorker.getRole().equals(Worker.Role.SELLER)) {
            response = statementRepository.findReqBranchTradeCountByResBranch(branchSeq);

        } else {

            response = statementRepository.findResBranchTradeCountByReqBranch(branchSeq);

        }

        return ResultTemplate.builder().status(HttpStatus.OK.value()).data(response).build();


    }

    public ResultTemplate getStatementDetail(UserDetails worker, Long statementSeq) {

        Long workerSeq = Long.parseLong(worker.getUsername());
        Worker myWorker = workerRepository.findById(workerSeq)
                .orElseThrow(() -> new NotFoundException(NotFoundException.WORKER_NOT_FOUND));

        Statement statement = statementRepository.findById(statementSeq).orElseThrow(() -> {
            throw new NotFoundException(NotFoundException.STATEMENT_NOT_FOUND);
        });

        Double totalPrice = 0.0;
        StatementReqInfoResponseDto reqInfo;
        StatementResInfoResponseDto resInfo;
        if (statement.getStatementStatus().equals(Statement.StatementStatus.WAITING)) {
            reqInfo = null;
            resInfo = null;

        } else if (statement.getStatementStatus().equals(Statement.StatementStatus.PREPARING)) {
            reqInfo = StatementReqInfoResponseDto.from(statement);
            resInfo = null;
        } else {
            reqInfo = StatementReqInfoResponseDto.from(statement);
            resInfo = StatementResInfoResponseDto.from(statement);
        }

        if (!((reqInfo != null && reqInfo.getCompanySeq() == myWorker.getCompany().getCompanySeq()) ||
              (resInfo != null && resInfo.getCompanySeq() == myWorker.getCompany().getCompanySeq()))) {
            throw new MismatchException(MismatchException.WORKER_AND_BRANCH_MISMATCH);
        }


        List<ItemResponseDto> itemList = statement.getItems().stream().map(item -> {
            return ItemResponseDto.from(item);
        }).collect(Collectors.toList());

        for (ItemResponseDto item : itemList) {
            totalPrice += item.getStockTotalPrice();
        }


        StatementDetailResponseDto responseDto = StatementDetailResponseDto.builder().reqInfo(reqInfo).resInfo(resInfo).
                statementSeq(statementSeq).itemList(itemList).totalPrice(totalPrice).tradeDate(statement.getTradeDate())
                .build();
        return ResultTemplate.builder().status(HttpStatus.OK.value()).data(responseDto).build();
    }

    public ResultTemplate getStatementListByFilterAndPagination(int page, LocalDateTime startDate,
                                                                LocalDateTime endDate, Long companySeq,
                                                                Long productSeq,
                                                                Statement.StatementStatus status) {

        PageRequest pageable = PageRequest.of(page, 5,
                Sort.by("statementSeq").ascending());
        Page<Statement> list = statementQueryRepository.findWithFilteringAndPagination(pageable, companySeq, status);


        List<WebStatementResponseDto> response = list.getContent()
                .stream()
                .map(statement -> {
                    AtomicReference<Double> price = new AtomicReference<>(0.0);

                    statement.getItems().forEach(item -> {
                        double itemTotalPrice = item.getStock().getStockPrice() * item.getStock().getStockQuantity();
                        price.updateAndGet(v -> v + itemTotalPrice);
                    });
                    return WebStatementResponseDto.from(statement, new DecimalFormat("#,###.00").format(price.get()));
                })
                .collect(Collectors.toList());

        return ResultTemplate.builder()
                .status(HttpStatus.OK.value())
                .data(response)
                .build();

    }


    public ResultTemplate<?> getStatementListPreparing(Long lastItemSeq, UserDetails worker) {
        // 유저 정보 가져오고
        Long workerSeq = Long.parseLong(worker.getUsername());
        Worker myWorker = workerRepository.findById(workerSeq)
                .orElseThrow(() -> new NotFoundException(NotFoundException.WORKER_NOT_FOUND));

        Long companySeq = myWorker.getCompany().getCompanySeq();

        List<Branch> branchList = null;

        // 내가 서명할 수 있는 브랜치만 남기고
        String myRole = myWorker.getRole().name();

        switch (myRole){
            case "PRODUCER":
                branchList = branchRepository.findByCompanySeqAndRole(companySeq, Collections.singletonList(Branch.BranchType.PRODUCT));
                break;
            case "OFFICIALS":
                branchList = branchRepository.findByCompanySeqAndRole(companySeq, Arrays.asList(Branch.BranchType.PROCESS, Branch.BranchType.PACKAGING));
                break;
            case "SELLER":
                branchList = branchRepository.findByCompanySeqAndRole(companySeq, Collections.singletonList(Branch.BranchType.SELL));
                break;
        }

        if (branchList == null || branchList.size() == 0){
            throw new NotFoundException(NotFoundException.BRANCH_NOT_FOUND);
        }

        List<Long> branchSeqList = branchList.stream()
                .map(Branch::getBranchSeq)
                .collect(Collectors.toList());
        // 무한스크롤 아님
        List<Statement> myStatement = statementRepository.findStatementsByBranchSeq(branchSeqList);

        StatementPreparingResponseDto responseDto = StatementPreparingResponseDto.builder()
                .statementList(
                        myStatement.stream().map(statement -> {

                            List<Stock> stocks = itemRepository.findStockByStatementSeq(statement.getStatementSeq());

                            String productsName = "";

                            if(stocks == null || stocks.size() == 0) {
                                throw new NotFoundException(NotFoundException.STOCK_NOT_FOUND);
                            } else if(stocks.size() == 1) {
                                productsName = stocks.get(0).getStockName();
                            } else {
                                productsName = stocks.get(0).getStockName() + " 외 " + String.valueOf(stocks.size()-1) + "건";
                            }

                            return PreparingListResponseDto.builder()
                                    .statementSeq(statement.getStatementSeq())
                                    .branchName(statement.getReqBranch().getBranchName())
                                    .productsName(productsName)
                                    .tradeDate(statement.getTradeDate())
                                    .build();

                        }).collect(Collectors.toList())
                )
                .hasNext(true)
                .build();

        return ResultTemplate.builder().status(200).data(responseDto).build();
    }


    // 거래 최초 등록
    @Transactional
    public ResultTemplate<?> addStatement(StatementCreateRequestDto request, UserDetails worker) {
        Long workerSeq = Long.parseLong(worker.getUsername());
        Worker myWorker = workerRepository.findById(workerSeq)
                .orElseThrow(() -> new NotFoundException(NotFoundException.WORKER_NOT_FOUND));

        Branch reqBranch = myWorker.getBranch();

        Branch resBranch = branchRepository.findById(request.getResponseBranch())
                .orElseThrow(() -> new NotFoundException("수급 관할 구역이" + NotFoundException.BRANCH_NOT_FOUND));

        if (reqBranch == resBranch) {
            throw new InvalidTradeException(InvalidTradeException.CANT_SAME_BRANCH);
        }

        Statement newStatement = Statement.createStatement(reqBranch, resBranch, Statement.StatementStatus.PREPARING, request.getTradeDate());
        statementRepository.save(newStatement);

        for (int i = 0; i < request.getItems().size(); i++) {
            Stock stock = stockRepository.findById(request.getItems().get(i))
                    .orElseThrow(() -> new NotFoundException(NotFoundException.STOCK_NOT_FOUND));
            Item newItem = Item.createItem(newStatement, stock);
            itemRepository.save(newItem);
        }

        return ResultTemplate.builder().status(200).data("거래 신청 완료").build();
    }


    // 거래명세서 서명
    @Transactional
    public ResultTemplate<?> signStatement(SignStatementRequestDto request, UserDetails worker) {
        Statement statement = statementRepository.findById(request.getStatementSeq())
                .orElseThrow(() -> new NotFoundException(NotFoundException.STATEMENT_NOT_FOUND));

        Long workerSeq = Long.parseLong(worker.getUsername());
        Worker myWorker = workerRepository.findById(workerSeq)
                .orElseThrow(() -> new NotFoundException(NotFoundException.WORKER_NOT_FOUND));

        if (request.getType().equals("SELL")) {
            if (!statement.getStatementStatus().name().equals("PREPARING")) {
                throw new InvalidTradeException(InvalidTradeException.NOT_SIGNING_PROCEDURE);
            }

            Branch myBranch = branchRepository.findById(statement.getReqBranch().getBranchSeq())
                    .orElseThrow(() -> new NotFoundException(NotFoundException.BRANCH_NOT_FOUND));

            if (myBranch.getCompany() != myWorker.getCompany()) {
                throw new MismatchException(MismatchException.WORKER_AND_BRANCH_MISMATCH);
            }

            statement.updateStatementSignFromReq(myWorker);
            statementRepository.save(statement);

        } else if (request.getType().equals("BUY")) {
            if (!statement.getStatementStatus().name().equals("WAITING")) {
                throw new InvalidTradeException(InvalidTradeException.NOT_SIGNING_PROCEDURE);
            }

            Branch myBranch = branchRepository.findById(statement.getResBranch().getBranchSeq())
                    .orElseThrow(() -> new NotFoundException(NotFoundException.BRANCH_NOT_FOUND));

            if (myBranch.getCompany() != myWorker.getCompany()) {
                throw new MismatchException(MismatchException.WORKER_AND_BRANCH_MISMATCH);
            }

            statement.updateStatementSignFromRes(myWorker);
            statementRepository.save(statement);

            adjustStockBaseOnStatement(statement);
        }

        return ResultTemplate.builder().status(200).data("서명이 완료되었습니다.").build();

    }

    public ResultTemplate<?> refuseStatement(RefuseStatementRequestDto request, UserDetails worker) {
        Statement statement = statementRepository.findById(request.getStatementSeq())
                .orElseThrow(() -> new NotFoundException(NotFoundException.STATEMENT_NOT_FOUND));

        Long workerSeq = Long.parseLong(worker.getUsername());
        Worker myWorker = workerRepository.findById(workerSeq)
                .orElseThrow(() -> new NotFoundException(NotFoundException.WORKER_NOT_FOUND));

        Branch myBranch = branchRepository.findById(statement.getResBranch().getBranchSeq())
                .orElseThrow(() -> new NotFoundException(NotFoundException.BRANCH_NOT_FOUND));

        if (myBranch.getCompany() != myWorker.getCompany()) {
            throw new MismatchException(MismatchException.WORKER_AND_BRANCH_MISMATCH);
        }

        if (statement.getStatementStatus() != Statement.StatementStatus.WAITING) {
            throw new InvalidTradeException(InvalidTradeException.NOT_REFUSING_PROCEDURE);
        }

        statement.updateStatementStatus(Statement.StatementStatus.REFUSAL);
        statementRepository.save(statement);

        return ResultTemplate.builder().status(200).data("해당 거래를 거절하였습니다.").build();
    }


    public void adjustStockBaseOnStatement(Statement statement) {
        Long statementSeq = statement.getStatementSeq();
        List<Stock> stocks = itemRepository.findStockByStatementSeq(statementSeq);

        Branch branch = statement.getResBranch();
        Branch fromBranch = statement.getReqBranch();

        for (Stock st : stocks) {
            if (!(st.getInOutStatus() == Stock.InOutStatus.OUT) || !(st.getUseStatus() == Stock.UseStatus.UNUSED)) {
                throw new InvalidTradeException(InvalidTradeException.INVALID_STOCK_FOR_TRADE);
            }

            st.updateUseStatus(Stock.UseStatus.USED);
            stockRepository.save(st);

            Stock newStock = Stock.createStock(
                    branch,
                    fromBranch,
                    st.getStockName(),
                    st.getStockCode() + branch.getChannelCode(),
                    st.getStockQuantity(),
                    st.getStockUnit(),
                    statement.getResDate(),
                    st.getStockPrice(),
                    Stock.InOutStatus.IN,
                    Stock.UseStatus.UNUSED);

            stockRepository.save(newStock);
        }
    }

}
