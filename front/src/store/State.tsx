import { atom } from "recoil";
import { recoilPersist } from "recoil-persist";

const { persistAtom } = recoilPersist();

type UserInfo = {
  accessToken: string | null;
  workerName: string | null;
  workerRole: string | null;
  branchSeq: number | null;
  branchName: string | null;
  branchType: string | null;
  companySeq: number | null;
  companyName: string | null;
};

type CompanyInfo = {
  companySeq: number | null;
  companyName: string | null;
  registrationNumber: string | null;
  companyLocation: string | null;
  companyContact: string | null;
  logoImage: string | null;
}

/** 유저 정보 저장 */
export const UserInfoState = atom<UserInfo>({
  key: "UserInfoState",
  default: {
    accessToken: null,
    workerName: null,
    workerRole: null,
    branchSeq: null,
    branchName: null,
    branchType: null,
    companySeq: null,
    companyName: null,
  },
  effects_UNSTABLE: [persistAtom],
});

/** 회사 정보 저장 */
export const CompanyInfoState = atom<CompanyInfo>({
  key: "CompanyInfoState",
  default: {
    companySeq: null,
    companyName: null,
    registrationNumber: null,
    companyLocation: null,
    companyContact: null,
    logoImage: null,
  },
  effects_UNSTABLE: [persistAtom],
});

