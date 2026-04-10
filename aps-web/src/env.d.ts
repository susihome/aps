declare module 'frappe-gantt' {
  export default class Gantt {
    constructor(wrapper: string | HTMLElement, tasks: object[], options?: Record<string, unknown>)
  }
}

declare module 'sockjs-client' {
  export default class SockJS {
    constructor(url: string, _reserved?: null, options?: Record<string, unknown>)
    close(): void
    onopen: (() => void) | null
    onmessage: ((e: { data: string }) => void) | null
    onclose: (() => void) | null
  }
}
