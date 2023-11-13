import styled, { keyframes } from "styled-components";
import { MainPaddingContainer } from "../../commons/style/mobileStyle/MobileLayoutStyle";
import TraderMainPageBtn from "../../components/organisms/trader/TraderMainPageBtn";
import TraderMainProfile from "../../components/organisms/trader/TraderMainProfile";
import TraderMainCount from "../../components/organisms/trader/TraderMainCount";
import { useNavigate } from "react-router-dom";
import {MdLogout} from "react-icons/md";

const TraderMainPage = () => {
  const navigate = useNavigate();

  const logout = () => {
    // 토큰 삭제
    localStorage.removeItem("recoil-persist");
    navigate('/m/login');
    window.location.reload();
  };

  return (
    <StyledContainer>
      <StyledLogout>
           <MdLogout size="1.5rem" color="#0a3145" onClick={logout}/>
      </StyledLogout>
      <TraderMainProfile />
      <MainPaddingContainer>
        <StyledAni2>
        <TraderMainCount />
        </StyledAni2>
        <TraderMainPageBtn />
      </MainPaddingContainer>
      <StyledFooter>Contact : 어쩌구 저쩌구</StyledFooter>
    </StyledContainer>
  );
};

export default TraderMainPage;

const fadeInUp = keyframes`
  from {
    opacity: 0;
    transform: translateY(2rem);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
`;

const fadeIn = keyframes`
   from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
`

const StyledContainer = styled.div`
  background-color: #ecf4ff;
  height: 100vh;
  
`;

const StyledFooter = styled.div`
  text-align: center;
  color: #ccc;
`;

const StyledLogout = styled.div`
  padding-top: 1rem;
  display: flex;
  justify-content: flex-end; 
`;

const StyledAni2 = styled.div`
  animation: ${fadeIn} 0.8s ease-out 0.4s both;
`
