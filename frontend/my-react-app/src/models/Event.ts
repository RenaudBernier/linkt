class Event {
    readonly id: number;
    readonly name: string;
    description: string;
    images: string[];
    price: number;
    date: Date;
    location: string;

    constructor(id: number, name: string, description: string, image: string, price: number, date: Date, location: string) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.image = image;
        this.price = price;
        this.date = date;
        this.location = location
    }

    // Getters
    public getName(): string {
        return this.name;
    }
    public getDescription(): string {
        return this.description;
    }
    public getImage(): string {
        return this.image;
    }
    public getPrice(): number {
        return this.price;
    }


    // example of the format Wednesday at 15 October 2025 at 17:00
    public getDate(): string {
        const date = this.date;

        return date.toLocaleString("en-GB", {
            weekday: "long",
            day: "2-digit",
            month: "long",
            year: "numeric",
            hour: "2-digit",
            minute: "2-digit",
            hour12: false
        }).replace(",", " at");
    }


    public getLocation(): string {
        return this.location;
    }

    // Setters
    public setDescription(description: string): void {
        this.description = description;
    }
    public setImage(image: string): void {
        this.image = image;
    }
    public setPrice(price: number): void {
        this.price = price;
    }
    public setDate(date: Date): void {
        this.date = date;
    }
    public setLocation(location: string): void {
        this.location = location;
    }

}



export default Event;
