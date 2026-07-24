export type LoginUserDto = {
  email: string;
  password: string;
};

export type RegisterUserDto = {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  phoneNumber: string;
  termsAccepted: boolean;
};
