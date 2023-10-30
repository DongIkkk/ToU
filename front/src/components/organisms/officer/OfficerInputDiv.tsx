import React from 'react';
import OfficerInput from '../../atoms/officer/OfficerInput';
import styled from 'styled-components';

interface InputDivProps {
  isInput: boolean;
}

const handleDropdownChange = (selectedValue: string) => {
  const intValue = parseInt(selectedValue, 10);
  // 나중에 이 int값을 seq넘버로 쓰면 됨
  console.log(intValue);
};

const OfficerInputDiv: React.FC<InputDivProps> = ({isInput}) => {
  if (isInput) {
    return (
      <MainDiv>
        <SubDiv>
            <StyledSpan>• 업체명</StyledSpan>
          <OfficerInput
            size={"underwriter"}
          />
        </SubDiv>
        <div>
          <SubDiv>
            <StyledSpan>• 담당자</StyledSpan>
            <OfficerInput
              size={"underwriter"}
            />
          </SubDiv>
          <SubDiv>
            <StyledSpan>• 연락처</StyledSpan>
            <OfficerInput
              size={"underwriter"}
            />
          </SubDiv>
        </div>
      </MainDiv>
    );
  } else {
    return (
      <MainDiv>
        <div>
          <SubDiv>
            <StyledSpan>• 품목명</StyledSpan>
            <OfficerInput
              size={"underwriter"}
            />
          </SubDiv>
          <SubDiv>
            <StyledSpan>• 수량/단위</StyledSpan>
            <OfficerInput
              size={"underwriter2"}
            />
            <Dropdown onChange={(e) => handleDropdownChange(e.target.value)}>
              <option value="1">kg</option>
              <option value="2">ton</option>
              <option value="3">마리</option>
            </Dropdown>
          </SubDiv>
        </div>
        <SubDiv>
          <StyledSpan>• 입고 일시</StyledSpan>
          <OfficerInput
            size={"underwriter"}
          />
        </SubDiv>
      </MainDiv>
    );
  }
  
};

export default OfficerInputDiv;

const MainDiv = styled.div`
  display: flex;
  border: 1px solid #666;
  margin: 10px;
  padding: 20px 0 0 0;
  font-size: 15px;
`

const SubDiv = styled.div`
  margin-bottom: 20px;
`

const StyledSpan = styled.span`
  display: inline-block;
  min-width: 80px;
  margin: 0 30px 0 20px;
`

const Dropdown = styled.select`
  /* width: 100%; */
  padding: 8px;
  position: relative;
  /* left: 30px; */
  font-size: 16px;
`
