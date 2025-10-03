interface User {
    userID : number;
    firstName : string;
    lastName : string;
    email : string;
    phoneNumber : string;

    login(email: string, password: string) : boolean;
    logout(): void
}




