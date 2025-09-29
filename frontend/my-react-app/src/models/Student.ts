// @ts-ignore
class Student {
    readonly userName: string;
    private readonly email: string;
    private readonly password: string;


    readonly purchasedEvents: Event[];
    readonly savedEvents: Event[];


    public constructor(username: string, email: string, password: string) {
        this.userName = username;
        this.email = email;
        this.password = password;
        this.purchasedEvents = [];
        this.savedEvents = [];
    }

    // Adding event to the purchasedEvents
    public addEvent(event: Event): void {
        this.purchasedEvents.push(event);
    }

    // Adding event to the savedEvents
    public saveEvent(event: Event): void {
        this.savedEvents.push(event);
    }


    public getUserName(): string {
        return this.userName;
    }


}