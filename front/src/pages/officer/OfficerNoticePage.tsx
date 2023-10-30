import { styled } from "styled-components";
import OfficerSideBar from "../../components/organisms/officer/OfficerSideBar";
import OfficerTitle from "../../components/atoms/officer/OfficerTitle";

const OfficerNoticePage = () => {
  return (
    <MainDiv>
      <OfficerSideBar/>
      <ContentDiv>
        <OfficerTitle>
          공지사항
        </OfficerTitle>
      </ContentDiv>
    </MainDiv>
  )
}

export default OfficerNoticePage

const MainDiv = styled.div`
  display: grid;
  grid-template-columns: 1fr 5fr;
  height: calc(100vh - 40px);
  overflow: hidden;
`

const ContentDiv = styled.div`
  padding: 20px;
  font-size: 17px;
  font-weight: bold;
  color: #545A96;
`
